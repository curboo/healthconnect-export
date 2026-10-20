# HC Export curbo 📱

[![CI](https://github.com/curboo/healthconnect-export/actions/workflows/build-apk.yml/badge.svg)](https://github.com/curboo/healthconnect-export/actions/workflows/build-apk.yml)
[![Release](https://img.shields.io/github/v/release/curboo/healthconnect-export)](https://github.com/curboo/healthconnect-export/releases)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![繁體中文](https://img.shields.io/badge/README-%E7%B9%81%E9%AB%94%E4%B8%AD%E6%96%87-blue)](README.md)

[繁體中文](README.md) ｜ **English**

Exports your Android **Health Connect** data (Samsung Health, Google Fit, and more) plus **phone screen time** to one JSON file per day, and pushes it automatically to your own server via webhook.

I built this version for my personal "Life Reset" habit program: the watch tracks sleep and workouts, the phone tracks how long I scroll, and the data reaches my Cloudflare Worker every 2 hours. My dashboard then decides on its own whether I slept on time or scrolled too much, with no manual check-ins.

> Forked from [kas-cor/healthconnect-export](https://github.com/kas-cor/healthconnect-export) (MIT). This fork fixes data-mapping bugs and adds new exports on top of the original work.

## Download

Get the latest `HCExport-curbo.apk` from [Releases](https://github.com/curboo/healthconnect-export/releases).

- App name: **HC Export curbo**
- Package: `com.healthconnect.export.curbo` (installs alongside the original)
- Requires Android 9 (API 28)+ with Health Connect available

## What this fork changes

### Fixes
- **Sleep stage mapping**: the original mapped Health Connect stage constants incorrectly (light sleep labelled REM, deep sleep labelled "out of bed"). Now follows the official constants: 1 Awake, 2 Sleeping, 3 Out of bed, 4 Light, 5 Deep, 6 REM, 7 Awake in bed.
- **Exercise type mapping**: uses the real Health Connect constants, so badminton, walking, strength training, etc. are labelled correctly.

### Additions
- **Sleep sessions**: `sleep.sessions[]` with start/end time, minutes asleep, minutes awake, and sleep latency, so bedtime and wake time can be read directly.
- **Body composition**: body fat, lean mass, body water, bone mass, BMR, height.
- **Historical data**: requests `READ_HEALTH_DATA_HISTORY`, so data older than 30 days before the first grant can be read (enable "Access past data" in Health Connect).
- **Hydration total**: `hydration.total_ml`.
- **Screen time**: `screen_time` with daily total minutes, unlock count, per-app minutes, and a 24-hour breakdown.
- **App usage timeline**: `screen_time.sessions[]` with the start/end of each foreground session (same-app gaps under 30 s merged, fragments under 10 s dropped), plus `unlock_times[]`.
- **Traditional Chinese UI**.

### Kept from the original
- One JSON file per day (`health_YYYY-MM-DD.json`), CSV optional
- Daily/weekly scheduled export, or **webhook every 2 hours**
- Webhook with Bearer token, retry, and one-tap test
- Schedule restored after reboot, Doze-proof watchdog, automatic catch-up of missed days
- Data source picker, date range, JSON viewer, dark mode

## Setup

1. Install the APK and grant Health Connect read permissions when prompted.
2. **Screen time**: enable Usage Access from the app's Settings. Android keeps only about 7–10 days of usage history.
3. **Historical data** (optional): enable "Access past data" in the Health Connect permissions screen.
4. In the Integrations tab, enter your webhook URL and token, pick the data types to send, and tap Test.
5. In the Schedule tab, turn on "Send every 2 hours". Setting the app's battery usage to Unrestricted is recommended.

## Output format (excerpt)

```json
{
  "date": "2026-10-20",
  "steps": { "total_steps": 6234 },
  "sleep": {
    "total_duration_minutes": 383,
    "asleep_minutes": 332,
    "awake_minutes": 33,
    "main_start": "2026-10-20T02:08:00+08:00",
    "main_end": "2026-10-20T08:31:00+08:00",
    "sleep_stages": { "Light sleep": 223, "Deep sleep": 53, "REM sleep": 56, "Awake": 33 },
    "sessions": [
      { "start": "2026-10-20T02:08:00+08:00", "end": "2026-10-20T08:31:00+08:00",
        "duration_minutes": 383, "asleep_minutes": 332, "awake_minutes": 33,
        "latency_minutes": 0, "source": "com.sec.android.app.shealth" }
    ]
  },
  "body_fat": { "percentage": 29.7 },
  "basal_metabolic_rate": { "kcal_per_day": 1692 },
  "hydration": { "total_ml": 1800 },
  "screen_time": {
    "total_minutes": 254,
    "unlocks": 99,
    "apps": [ { "package": "com.instagram.barcelona", "label": "Threads", "minutes": 38, "hours": [0, 0, 0, "…"] } ],
    "sessions": [ { "package": "com.instagram.barcelona", "label": "Threads", "start": "11:15:05", "end": "11:16:18", "seconds": 73 } ],
    "unlock_times": ["11:14", "11:22", "…"]
  }
}
```

Fields depend on what your watch and apps write to Health Connect; missing data is `null` or omitted.

## Webhook

| Item | Value |
|---|---|
| Method | `POST` |
| Content-Type | `application/json` |
| Auth | `Authorization: Bearer <token>` (optional) |
| Timeout | 15 s |
| Retry | 1 retry after 1 s on 5xx/network errors |

The same day is sent many times (every 2 hours). Receivers should **merge** by date, letting non-null new values win, so a later partial payload never wipes earlier complete data.

## My pipeline

```
Galaxy Watch → Samsung Health → Health Connect ─┐
Android Usage Access ───────────────────────────┤
                                                ▼
                       HC Export curbo (every 2 hours)
                                                │ POST JSON
                                                ▼
                  Cloudflare Worker → KV (one entry per day, merged)
                                                │
                       ┌────────────────────────┴───────────┐
                       ▼                                    ▼
              Private dashboard (PWA)            Daily auto-scoring / Google Drive backup
```

## Build

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Unit tests
./gradlew testDebugUnitTest
```

Pushing a `v*` tag (e.g. `v1.14-curbo.0`) makes GitHub Actions build a signed APK and publish it to Releases.

```bash
git tag v1.14-curbo.1
git push origin v1.14-curbo.1
```

## Versions

| Version | Changes |
|---|---|
| v1.14-curbo.0 | App usage timeline `screen_time.sessions[]`, unlock times |
| v1.13-curbo.0 | Screen time `screen_time`, hydration `hydration.total_ml` |
| v1.12-curbo.0 | Historical data permission |
| v1.11-curbo.0 | Traditional Chinese UI, body composition and BMR, fixed signing key |
| v1.10-curbo.x | Sleep stage and exercise type fixes, sleep session times |

See [CHANGELOG.md](CHANGELOG.md) for the original project's full history.

## Privacy

Data stays on your phone, or goes only to the webhook / Google Drive **you** configure. Nothing is sent to any third party.

## License

MIT License. Original code © kas-cor, modifications © curbo (劉宏昱). See [LICENSE](LICENSE).
