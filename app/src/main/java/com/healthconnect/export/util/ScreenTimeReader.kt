package com.healthconnect.export.util

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import com.healthconnect.export.data.ScreenTimeAppData
import com.healthconnect.export.data.ScreenTimeData
import com.healthconnect.export.data.ScreenSessionData
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Phone screen time from UsageStatsManager (requires the special "Usage access"
 * permission, PACKAGE_USAGE_STATS, granted by the user in system settings).
 *
 * Foreground time is rebuilt from raw ACTIVITY_RESUMED / ACTIVITY_PAUSED events
 * (same int values as the older MOVE_TO_FOREGROUND / MOVE_TO_BACKGROUND), clipped
 * at local day and hour boundaries of the device time zone, the same zone the
 * rest of the export uses for its "date" key.
 *
 * Android keeps detailed events only for roughly 7–10 days, so older days come
 * back empty and are simply omitted.
 */
object ScreenTimeReader {
    private const val TAG = "ScreenTimeReader"

    // UsageEvents.Event constants (raw values, stable since API 21/28/29)
    private const val ACTIVITY_RESUMED = 1 // == MOVE_TO_FOREGROUND
    private const val ACTIVITY_PAUSED = 2 // == MOVE_TO_BACKGROUND
    private const val SCREEN_NON_INTERACTIVE = 16
    private const val KEYGUARD_SHOWN = 17
    private const val KEYGUARD_HIDDEN = 18
    private const val ACTIVITY_STOPPED = 23
    private const val DEVICE_SHUTDOWN = 26

    /** Look back before the window start so an app already open at 00:00 is counted. */
    private const val LOOKBACK_MS = 6L * 60 * 60 * 1000

    private const val MINUTE_MS = 60_000L

    /** Same-app sessions separated by less than this are merged on the timeline. */
    private const val MERGE_GAP_MS = 30_000L

    /** Timeline sessions shorter than this are dropped (still counted in totals). */
    private const val MIN_SESSION_MS = 10_000L

    private val HMS = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
    private val HM = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

    private val ALWAYS_EXCLUDED =
        setOf(
            "com.android.systemui",
            "android",
        )

    fun hasUsageAccess(context: Context): Boolean =
        runCatching {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
                } else {
                    @Suppress("DEPRECATION")
                    appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
                }
            if (mode == AppOpsManager.MODE_DEFAULT) {
                context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) ==
                    PackageManager.PERMISSION_GRANTED
            } else {
                mode == AppOpsManager.MODE_ALLOWED
            }
        }.getOrDefault(false)

    fun usageAccessSettingsIntent(): Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    private fun launcherPackages(context: Context): Set<String> =
        runCatching {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            @Suppress("DEPRECATION")
            context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_ALL)
                .mapNotNull { it.activityInfo?.packageName }
                .toSet()
        }.getOrDefault(emptySet())

    private fun appLabel(
        context: Context,
        pkg: String,
    ): String =
        runCatching {
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            val info = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(info).toString()
        }.getOrDefault(pkg)

    /** One foreground interval of a package. */
    internal data class Session(
        val pkg: String,
        val start: Long,
        val end: Long,
    )

    /**
     * Returns screen time per local date (yyyy-MM-dd) for [startDate]..[endDate].
     * Days with no usage data at all are absent from the map.
     */
    fun readDays(
        context: Context,
        startDate: LocalDate,
        endDate: LocalDate,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Map<String, ScreenTimeData> {
        if (!hasUsageAccess(context)) return emptyMap()
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyMap()

        val windowStart = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val windowEndFull = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()
        val windowEnd = minOf(windowEndFull, now)
        if (windowEnd <= windowStart) return emptyMap()

        val sessions = mutableListOf<Session>()
        val unlockTimes = mutableListOf<Long>()
        var sawKeyguardEvents = false

        try {
            val events = usm.queryEvents(windowStart - LOOKBACK_MS, windowEnd)
            val ev = UsageEvents.Event()
            var currentPkg: String? = null
            var currentStart = 0L
            val openActivities = mutableSetOf<String>()

            fun closeCurrent(at: Long) {
                val pkg = currentPkg ?: return
                if (at > currentStart) sessions.add(Session(pkg, currentStart, at))
                currentPkg = null
                openActivities.clear()
            }

            while (events.hasNextEvent()) {
                events.getNextEvent(ev)
                val t = ev.timeStamp
                val pkg = ev.packageName ?: continue
                when (ev.eventType) {
                    ACTIVITY_RESUMED -> {
                        if (currentPkg != pkg) {
                            // Only one app is in the foreground at a time: a resume of
                            // another package ends the previous session even if its
                            // pause event was lost.
                            closeCurrent(t)
                            currentPkg = pkg
                            currentStart = t
                        }
                        openActivities.add(ev.className ?: "")
                    }
                    ACTIVITY_PAUSED, ACTIVITY_STOPPED -> {
                        if (currentPkg == pkg) {
                            openActivities.remove(ev.className ?: "")
                            if (openActivities.isEmpty()) closeCurrent(t)
                        }
                    }
                    SCREEN_NON_INTERACTIVE, DEVICE_SHUTDOWN -> closeCurrent(t)
                    KEYGUARD_SHOWN -> sawKeyguardEvents = true
                    KEYGUARD_HIDDEN -> {
                        sawKeyguardEvents = true
                        unlockTimes.add(t)
                    }
                }
            }
            // App still in the foreground at the end of the range.
            closeCurrent(windowEnd)
        } catch (e: Exception) {
            Log.w(TAG, "queryEvents failed", e)
            return emptyMap()
        }

        val excluded = ALWAYS_EXCLUDED + launcherPackages(context) + context.packageName
        return aggregate(
            sessions = sessions.filter { it.pkg !in excluded },
            unlockTimes = unlockTimes,
            unlocksAvailable = sawKeyguardEvents,
            startDate = startDate,
            endDate = endDate,
            zone = zone,
            windowStart = windowStart,
            windowEnd = windowEnd,
            label = { appLabel(context, it) },
        )
    }

    /** Pure aggregation: sessions → per-day / per-app / per-hour minutes. */
    internal fun aggregate(
        sessions: List<Session>,
        unlockTimes: List<Long>,
        unlocksAvailable: Boolean,
        startDate: LocalDate,
        endDate: LocalDate,
        zone: ZoneId,
        windowStart: Long,
        windowEnd: Long,
        label: (String) -> String,
    ): Map<String, ScreenTimeData> {
        // date -> pkg -> 24 hourly buckets in ms
        val perDay = mutableMapOf<String, MutableMap<String, LongArray>>()

        sessions.forEach { s ->
            var cursor = maxOf(s.start, windowStart)
            val end = minOf(s.end, windowEnd)
            while (cursor < end) {
                val zdt = Instant.ofEpochMilli(cursor).atZone(zone)
                val hourStart = zdt.withMinute(0).withSecond(0).withNano(0)
                val nextHour = hourStart.plusHours(1).toInstant().toEpochMilli()
                val sliceEnd = minOf(end, nextHour)
                val date = zdt.toLocalDate().toString()
                val buckets = perDay.getOrPut(date) { mutableMapOf() }.getOrPut(s.pkg) { LongArray(24) }
                buckets[zdt.hour] += sliceEnd - cursor
                cursor = sliceEnd
            }
        }

        val unlocksPerDay =
            unlockTimes
                .filter { it in windowStart until windowEnd }
                .groupingBy { Instant.ofEpochMilli(it).atZone(zone).toLocalDate().toString() }
                .eachCount()

        val labels = mutableMapOf<String, String>()
        val result = mutableMapOf<String, ScreenTimeData>()

        // Timeline: merge same-app sessions with tiny gaps, clip to window, split at local midnight.
        val timeline = mutableMapOf<String, MutableList<ScreenSessionData>>()
        val merged = mutableListOf<Session>()
        sessions.sortedBy { it.start }.forEach { s ->
            val st = maxOf(s.start, windowStart)
            val en = minOf(s.end, windowEnd)
            if (en <= st) return@forEach
            val last = merged.lastOrNull()
            if (last != null && last.pkg == s.pkg && st - last.end < MERGE_GAP_MS) {
                merged[merged.size - 1] = last.copy(end = maxOf(last.end, en))
            } else {
                merged.add(Session(s.pkg, st, en))
            }
        }
        merged.forEach { s ->
            var cursor = s.start
            while (cursor < s.end) {
                val zdt = Instant.ofEpochMilli(cursor).atZone(zone)
                val midnight = zdt.toLocalDate().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val sliceEnd = minOf(s.end, midnight)
                if (sliceEnd - cursor >= MIN_SESSION_MS) {
                    val endZ = Instant.ofEpochMilli(sliceEnd).atZone(zone)
                    timeline.getOrPut(zdt.toLocalDate().toString()) { mutableListOf() }.add(
                        ScreenSessionData(
                            packageName = s.pkg,
                            label = labels.getOrPut(s.pkg) { label(s.pkg) },
                            start = zdt.format(HMS),
                            end = if (sliceEnd == midnight) "24:00:00" else endZ.format(HMS),
                            seconds = (sliceEnd - cursor) / 1000,
                        ),
                    )
                }
                cursor = sliceEnd
            }
        }
        val unlockTimesPerDay =
            unlockTimes
                .filter { it in windowStart until windowEnd }
                .sorted()
                .groupBy({ Instant.ofEpochMilli(it).atZone(zone).toLocalDate().toString() }) {
                    Instant.ofEpochMilli(it).atZone(zone).format(HM)
                }
        var day = startDate
        while (!day.isAfter(endDate)) {
            val key = day.toString()
            val apps = perDay[key].orEmpty()
            val unlocks = unlocksPerDay[key]
            val totalMs = apps.values.sumOf { it.sum() }
            if (totalMs > 0 || unlocks != null) {
                val appList =
                    apps
                        .map { (pkg, buckets) -> Triple(pkg, buckets, buckets.sum()) }
                        .filter { it.third >= MINUTE_MS }
                        .sortedByDescending { it.third }
                        .map { (pkg, buckets, ms) ->
                            ScreenTimeAppData(
                                packageName = pkg,
                                label = labels.getOrPut(pkg) { label(pkg) },
                                minutes = Math.round(ms.toDouble() / MINUTE_MS),
                                hours = buckets.map { Math.round(it.toDouble() / MINUTE_MS).toInt() },
                            )
                        }
                result[key] =
                    ScreenTimeData(
                        totalMinutes = Math.round(totalMs.toDouble() / MINUTE_MS),
                        unlocks = if (unlocksAvailable) (unlocks ?: 0) else null,
                        apps = appList,
                        sessions = timeline[key].orEmpty(),
                        unlockTimes = if (unlocksAvailable) unlockTimesPerDay[key].orEmpty() else emptyList(),
                    )
            }
            day = day.plusDays(1)
        }
        return result
    }
}
