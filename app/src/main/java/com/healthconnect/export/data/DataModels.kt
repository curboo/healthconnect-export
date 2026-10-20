package com.healthconnect.export.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ===== Core data classes for Health Connect =====

@Serializable
data class DailyHealthRecord(
    @SerialName("date") val date: String,
    @SerialName("steps") val steps: StepsData? = null,
    @SerialName("heart_rate") val heartRate: HeartRateData? = null,
    @SerialName("sleep") val sleep: SleepData? = null,
    @SerialName("calories") val calories: CaloriesData? = null,
    @SerialName("distance") val distance: DistanceData? = null,
    @SerialName("floors_climbed") val floorsClimbed: FloorsClimbedData? = null,
    @SerialName("active_calories") val activeCalories: ActiveCaloriesData? = null,
    @SerialName("weight") val weight: WeightData? = null,
    @SerialName("body_fat") val bodyFat: BodyFatData? = null,
    @SerialName("blood_pressure") val bloodPressure: BloodPressureData? = null,
    @SerialName("blood_glucose") val bloodGlucose: BloodGlucoseData? = null,
    @SerialName("oxygen_saturation") val oxygenSaturation: OxygenSaturationData? = null,
    @SerialName("body_temperature") val bodyTemperature: BodyTemperatureData? = null,
    @SerialName("respiratory_rate") val respiratoryRate: RespiratoryRateData? = null,
    @SerialName("hydration") val hydration: HydrationData? = null,
    @SerialName("resting_heart_rate") val restingHeartRate: RestingHeartRateData? = null,
    @SerialName("exercises") val exercises: List<ExerciseData>? = null,
    @SerialName("nutrition") val nutrition: List<NutritionData>? = null,
    @SerialName("speed") val speed: SpeedData? = null,
    @SerialName("menstruation") val menstruation: MenstruationData? = null,
    @SerialName("lean_body_mass") val leanBodyMass: MassData? = null,
    @SerialName("body_water_mass") val bodyWaterMass: MassData? = null,
    @SerialName("bone_mass") val boneMass: MassData? = null,
    @SerialName("basal_metabolic_rate") val basalMetabolicRate: BmrData? = null,
    @SerialName("heart_rate_variability") val hrv: ValueData? = null,
    @SerialName("vo2_max") val vo2Max: ValueData? = null,
    @SerialName("height") val height: ValueData? = null,
    @SerialName("elevation_gained") val elevationGained: ValueData? = null,
    @SerialName("power") val power: ValueData? = null,
    @SerialName("steps_cadence") val stepsCadence: ValueData? = null,
    @SerialName("screen_time") val screenTime: ScreenTimeData? = null,
    @SerialName("metadata") val metadata: ExportMetadata,
)

@Serializable
data class StepsData(
    @SerialName("total_steps") val totalSteps: Long,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class HeartRateData(
    @SerialName("avg_bpm") val avgBpm: Double,
    @SerialName("min_bpm") val minBpm: Int,
    @SerialName("max_bpm") val maxBpm: Int,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class SleepData(
    @SerialName("total_duration_minutes") val totalDurationMinutes: Long,
    @SerialName("sleep_stages") val sleepStages: Map<String, Long>,
    @SerialName("records_count") val recordsCount: Int,
    @SerialName("asleep_minutes") val asleepMinutes: Long? = null,
    @SerialName("awake_minutes") val awakeMinutes: Long? = null,
    @SerialName("main_start") val mainStart: String? = null,
    @SerialName("main_end") val mainEnd: String? = null,
    @SerialName("main_latency_minutes") val mainLatencyMinutes: Long? = null,
    @SerialName("sessions") val sessions: List<SleepSessionData> = emptyList(),
)

@Serializable
data class SleepSessionData(
    @SerialName("start") val start: String,
    @SerialName("end") val end: String,
    @SerialName("duration_minutes") val durationMinutes: Long,
    @SerialName("asleep_minutes") val asleepMinutes: Long,
    @SerialName("awake_minutes") val awakeMinutes: Long,
    @SerialName("latency_minutes") val latencyMinutes: Long? = null,
    @SerialName("stages") val stages: Map<String, Long> = emptyMap(),
    @SerialName("source") val source: String? = null,
)

@Serializable
data class CaloriesData(
    @SerialName("total_calories_kcal") val totalCalories: Double,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class DistanceData(
    @SerialName("total_distance_meters") val totalDistanceMeters: Double,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class FloorsClimbedData(
    @SerialName("total_floors") val totalFloors: Double,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class ActiveCaloriesData(
    @SerialName("total_calories_kcal") val totalCalories: Double,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class WeightData(
    @SerialName("weight_kg") val weightKg: Double,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class MassData(
    @SerialName("kg") val kg: Double,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class ValueData(
    @SerialName("value") val value: Double,
    @SerialName("unit") val unit: String,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class BmrData(
    @SerialName("kcal_per_day") val kcalPerDay: Double,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class BodyFatData(
    @SerialName("percentage") val percentage: Double,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class BloodPressureData(
    @SerialName("systolic_mmhg") val systolicMmHg: Double,
    @SerialName("diastolic_mmhg") val diastolicMmHg: Double,
    @SerialName("body_position") val bodyPosition: String? = null,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class BloodGlucoseData(
    @SerialName("level_mmol_per_l") val level: Double,
    @SerialName("specimen_source") val specimenSource: String? = null,
    @SerialName("meal_type") val mealType: String? = null,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class OxygenSaturationData(
    @SerialName("percentage") val percentage: Double,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class BodyTemperatureData(
    @SerialName("temperature_celsius") val temperatureCelsius: Double,
    @SerialName("measurement_location") val measurementLocation: String? = null,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class RespiratoryRateData(
    @SerialName("rate") val rate: Double,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class HydrationData(
    @SerialName("total_volume_liters") val totalVolumeLiters: Double,
    @SerialName("records_count") val recordsCount: Int,
    @SerialName("total_ml") val totalMl: Long? = null,
)

/** Phone screen time for one local day, from UsageStatsManager. */
@Serializable
data class ScreenTimeData(
    @SerialName("total_minutes") val totalMinutes: Long,
    @SerialName("unlocks") val unlocks: Int? = null,
    @SerialName("apps") val apps: List<ScreenTimeAppData> = emptyList(),
    /** Foreground sessions in local time order (timeline). */
    @SerialName("sessions") val sessions: List<ScreenSessionData> = emptyList(),
    /** Local times (HH:mm) the keyguard was dismissed. */
    @SerialName("unlock_times") val unlockTimes: List<String> = emptyList(),
)

@Serializable
data class ScreenSessionData(
    @SerialName("package") val packageName: String,
    @SerialName("label") val label: String,
    /** Local start / end, HH:mm:ss. */
    @SerialName("start") val start: String,
    @SerialName("end") val end: String,
    @SerialName("seconds") val seconds: Long,
)

@Serializable
data class ScreenTimeAppData(
    @SerialName("package") val packageName: String,
    @SerialName("label") val label: String,
    @SerialName("minutes") val minutes: Long,
    /** Foreground minutes per local hour 0–23. */
    @SerialName("hours") val hours: List<Int>,
)

@Serializable
data class RestingHeartRateData(
    @SerialName("avg_bpm") val avgBpm: Double,
    @SerialName("min_bpm") val minBpm: Long,
    @SerialName("max_bpm") val maxBpm: Long,
    @SerialName("records_count") val recordsCount: Int,
)

@Serializable
data class ExerciseData(
    @SerialName("exercise_type") val exerciseType: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("duration_minutes") val durationMinutes: Long,
    @SerialName("title") val title: String? = null,
    @SerialName("notes") val notes: String? = null,
)

@Serializable
data class NutritionData(
    @SerialName("name") val name: String? = null,
    @SerialName("meal_type") val mealType: String? = null,
    @SerialName("energy_kcal") val energyKcal: Double? = null,
    @SerialName("protein_g") val proteinG: Double? = null,
    @SerialName("total_carbohydrate_g") val totalCarbohydrateG: Double? = null,
    @SerialName("fat_g") val fatG: Double? = null,
    @SerialName("dietary_fiber_g") val dietaryFiberG: Double? = null,
    @SerialName("sugar_g") val sugarG: Double? = null,
    @SerialName("saturated_fat_g") val saturatedFatG: Double? = null,
    @SerialName("trans_fat_g") val transFatG: Double? = null,
    @SerialName("cholesterol_mg") val cholesterolMg: Double? = null,
    @SerialName("sodium_mg") val sodiumMg: Double? = null,
    @SerialName("potassium_mg") val potassiumMg: Double? = null,
    @SerialName("caffeine_mg") val caffeineMg: Double? = null,
    @SerialName("calcium_mg") val calciumMg: Double? = null,
    @SerialName("iron_mg") val ironMg: Double? = null,
    @SerialName("magnesium_mg") val magnesiumMg: Double? = null,
    @SerialName("vitamin_c_mg") val vitaminCMg: Double? = null,
    @SerialName("vitamin_d_mcg") val vitaminDMcg: Double? = null,
)

@Serializable
data class MenstruationData(
    @SerialName("flow_type") val flowType: String? = null,
    @SerialName("time") val time: String,
)

@Serializable
data class SpeedData(
    @SerialName("avg_speed_meters_per_second") val avgSpeedMetersPerSecond: Double? = null,
    @SerialName("records_count") val recordsCount: Int = 0,
)

@Serializable
data class ExportMetadata(
    @SerialName("app_version") val appVersion: String,
    @SerialName("export_timestamp") val exportTimestamp: String,
    @SerialName("timezone") val timezone: String,
    @SerialName("source_device") val sourceDevice: String? = null,
)

// ===== Export Configuration =====

@Serializable
enum class ExportFrequency(
    val displayName: String,
    val hours: Long,
) {
    MANUAL("Manual", 0),
    DAILY("Daily", 24),
    WEEKLY("Weekly", 168),
}

/**
 * Output format for exported health data files.
 */
@Serializable
enum class ExportFormat(
    val displayName: String,
) {
    JSON("JSON"),
    CSV("CSV"),
}

/**
 * Preset period selection for the export date range.
 *
 * When [LAST_7_DAYS] or [LAST_30_DAYS] is active, the actual start/end dates are
 * recomputed from the current date on every launch and before each export, so the
 * window keeps sliding instead of freezing on the dates first picked.
 */
@Serializable
enum class DateRangePreset(
    val displayName: String,
) {
    NONE("Custom"),
    LAST_7_DAYS("Last 7 days"),
    LAST_30_DAYS("Last 30 days"),
    ;

    /**
     * Computes the [start, end] date window for this preset relative to [referenceDate].
     * 7 days → last 7 days including today; 30 days → last 30 days including today.
     * [NONE] is a custom range and has no meaning here — callers must handle it separately.
     */
    fun calcRange(referenceDate: LocalDate): Pair<LocalDate, LocalDate> =
        when (this) {
            LAST_7_DAYS -> referenceDate.minusDays(6) to referenceDate
            LAST_30_DAYS -> referenceDate.minusDays(29) to referenceDate
            NONE -> referenceDate to referenceDate
        }
}

@Serializable
data class ExportConfig(
    val enabledTypes: Set<HealthDataType>,
    val frequency: ExportFrequency,
    val autoSyncDrive: Boolean,
    val webhookUrl: String = "",
    val webhookAuthToken: String = "",
    val autoSendWebhook: Boolean = false,
    val autoSendWebhookEvery2Hours: Boolean = false,
    val outputDirectory: String = "HealthConnectExport",
    val selectedSourcePackage: String? = null,
    val exportFormat: ExportFormat = ExportFormat.JSON,
    val scheduleHour: Int? = null,
)

@Serializable
enum class HealthDataType(
    val displayName: String,
) {
    STEPS("Steps"),
    HEART_RATE("Heart Rate"),
    SLEEP("Sleep"),
    CALORIES("Calories"),
    DISTANCE("Distance"),
    FLOORS_CLIMBED("Floors Climbed"),
    ACTIVE_CALORIES("Active Calories"),
    WEIGHT("Weight"),
    BODY_FAT("Body Fat"),
    BLOOD_PRESSURE("Blood Pressure"),
    BLOOD_GLUCOSE("Blood Glucose"),
    OXYGEN_SATURATION("Oxygen Saturation"),
    BODY_TEMPERATURE("Body Temperature"),
    RESPIRATORY_RATE("Respiratory Rate"),
    HYDRATION("Hydration"),
    RESTING_HEART_RATE("Resting Heart Rate"),
    EXERCISE("Exercise"),
    NUTRITION("Nutrition"),
    SPEED("Speed"),
    MENSTRUATION("Menstruation"),
    LEAN_BODY_MASS("Lean Body Mass"),
    BODY_WATER_MASS("Body Water Mass"),
    BONE_MASS("Bone Mass"),
    BASAL_METABOLIC_RATE("Basal Metabolic Rate"),
    HRV("Heart Rate Variability"),
    VO2_MAX("VO2 Max"),
    HEIGHT("Height"),
    ELEVATION_GAINED("Elevation Gained"),
    POWER("Power"),
    STEPS_CADENCE("Steps Cadence"),
}

// ===== Health Connect Int constant → Human-readable string mappers =====

fun bodyPositionToString(value: Int?): String? {
    if (value == null) return null
    return when (value) {
        0 -> "Unknown"
        1 -> "Standing"
        2 -> "Sitting"
        3 -> "Lying down"
        4 -> "Reclining"
        else -> "Other ($value)"
    }
}

fun specimenSourceToString(value: Int?): String? {
    if (value == null) return null
    return when (value) {
        0 -> "Unknown"
        1 -> "Interstitial fluid"
        2 -> "Capillary blood"
        3 -> "Plasma"
        4 -> "Serum"
        5 -> "Whole blood"
        else -> "Other ($value)"
    }
}

fun mealTypeToString(value: Int?): String? {
    if (value == null) return null
    return when (value) {
        0 -> "Unknown"
        1 -> "Fasting"
        2 -> "Before meal"
        3 -> "After meal"
        4 -> "General"
        else -> "Other ($value)"
    }
}

fun sleepStageToString(value: Int?): String? {
    if (value == null) return null
    return when (value) {
        // androidx.health.connect.client.records.SleepSessionRecord.STAGE_TYPE_*
        0 -> "Unknown"
        1 -> "Awake"
        2 -> "Sleeping"
        3 -> "Out of bed"
        4 -> "Light sleep"
        5 -> "Deep sleep"
        6 -> "REM sleep"
        7 -> "Awake in bed"
        else -> "Other ($value)"
    }
}

fun measurementLocationToString(value: Int?): String? {
    if (value == null) return null
    return when (value) {
        0 -> "Unknown"
        1 -> "Axillary"
        2 -> "Body"
        3 -> "Ear"
        4 -> "Finger"
        5 -> "Forehead"
        6 -> "Mouth"
        7 -> "Rectal"
        8 -> "Toe"
        9 -> "Tympanic"
        10 -> "Wrist"
        11 -> "Temporal artery"
        else -> "Other ($value)"
    }
}

fun menstruationFlowToString(value: Int?): String? {
    if (value == null) return null
    return when (value) {
        0 -> "Unknown"
        1 -> "Light"
        2 -> "Medium"
        3 -> "Heavy"
        else -> "Other ($value)"
    }
}

fun nutritionMealTypeToString(value: Int?): String? {
    if (value == null) return null
    return when (value) {
        0 -> "Unknown"
        1 -> "Breakfast"
        2 -> "Lunch"
        3 -> "Dinner"
        4 -> "Snack"
        else -> "Other ($value)"
    }
}

fun exerciseTypeToString(value: Int?): String? {
    if (value == null) return null
    // androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_*
    return when (value) {
        0 -> "Other workout"
        2 -> "Badminton"
        4 -> "Baseball"
        5 -> "Basketball"
        8 -> "Biking"
        9 -> "Stationary biking"
        10 -> "Boot camp"
        11 -> "Boxing"
        13 -> "Calisthenics"
        14 -> "Cricket"
        16 -> "Dancing"
        25 -> "Elliptical"
        26 -> "Exercise class"
        27 -> "Fencing"
        28 -> "American football"
        29 -> "Australian football"
        31 -> "Frisbee"
        32 -> "Golf"
        33 -> "Guided breathing"
        34 -> "Gymnastics"
        35 -> "Handball"
        36 -> "HIIT"
        37 -> "Hiking"
        38 -> "Ice hockey"
        39 -> "Ice skating"
        44 -> "Martial arts"
        46 -> "Paddling"
        47 -> "Paragliding"
        48 -> "Pilates"
        50 -> "Racquetball"
        51 -> "Rock climbing"
        52 -> "Roller hockey"
        53 -> "Rowing"
        54 -> "Rowing machine"
        55 -> "Rugby"
        56 -> "Running"
        57 -> "Treadmill running"
        58 -> "Sailing"
        59 -> "Scuba diving"
        60 -> "Skating"
        61 -> "Skiing"
        62 -> "Snowboarding"
        63 -> "Snowshoeing"
        64 -> "Soccer"
        65 -> "Softball"
        66 -> "Squash"
        68 -> "Stair climbing"
        69 -> "Stair climbing machine"
        70 -> "Strength training"
        71 -> "Stretching"
        72 -> "Surfing"
        73 -> "Open water swimming"
        74 -> "Pool swimming"
        75 -> "Table tennis"
        76 -> "Tennis"
        78 -> "Volleyball"
        79 -> "Walking"
        80 -> "Water polo"
        81 -> "Weightlifting"
        82 -> "Wheelchair"
        83 -> "Yoga"
        else -> "Other ($value)"
    }
}

/**
 * Map of known health data source packages to human-readable display names.
 * Used for UI selection and filtering.
 */
val KNOWN_SOURCE_PACKAGES: Map<String, String> =
    mapOf(
        "com.mi.health" to "Xiaomi Mi Fitness",
        "com.xiaomi.hm.health" to "Xiaomi Wear",
        "com.google.android.apps.fitness" to "Google Fit",
        "com.samsung.samsunghealth" to "Samsung Health",
        "com.samsung.android.wearable.health" to "Samsung Health (Wear)",
        "com.fitbit.FitbitMobile" to "Fitbit",
        "com.mobvoi.companion.at" to "Mobvoi / TicWatch",
        "com.huawei.health" to "Huawei Health",
        "com.hmdm.wearable.health" to "Nokia Health",
        "com.sec.android.app.shealth" to "Samsung S Health",
        "com.htc.fitness" to "HTC Fitness",
        "com.sonymobile.advancedwidget.health" to "Sony Health",
        "com.google.android.wearable.app" to "Google Wear OS",
    )

/**
 * Returns a human-readable name for a source package, or the raw package name if unknown.
 */
fun sourceDisplayName(packageName: String): String = KNOWN_SOURCE_PACKAGES[packageName] ?: packageName

// ===== Export Summary =====

/**
 * Dashboard summary computed after export.
 */
data class ExportSummary(
    val totalSteps: Long = 0,
    val avgHeartRate: Double = 0.0,
    val totalCalories: Double = 0.0,
    val totalDistanceMeters: Double = 0.0,
    val avgSleepMinutes: Long = 0,
    val totalActiveCalories: Double = 0.0,
    val daysCount: Int = 0,
    val startDate: String = "",
    val endDate: String = "",
)

// ===== Helpers =====

fun Instant.toDateString(): String = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault()).format(this)

fun Instant.toDateTimeString(): String = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()).format(this)
