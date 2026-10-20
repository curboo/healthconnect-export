# HC Export curbo 📱

[![CI](https://github.com/curboo/healthconnect-export/actions/workflows/build-apk.yml/badge.svg)](https://github.com/curboo/healthconnect-export/actions/workflows/build-apk.yml)
[![Release](https://img.shields.io/github/v/release/curboo/healthconnect-export)](https://github.com/curboo/healthconnect-export/releases)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![English](https://img.shields.io/badge/README-English-blue)](README.en.md)

**繁體中文** ｜ [English](README.en.md)

把 Android **Health Connect**（Samsung Health、Google Fit 等）的健康資料，加上**手機使用時間**，每天匯出成一份 JSON，並自動用 Webhook 傳到你自己的伺服器。

這是我為個人「Life Reset」習慣計畫打造的版本：手錶記錄睡眠與運動、手機記錄滑了多久，資料每 2 小時送到我的 Cloudflare Worker，儀表板就能自動判斷「今天有沒有準時睡、有沒有滑太多」，不用手動打卡。

> 本專案 fork 自 [kas-cor/healthconnect-export](https://github.com/kas-cor/healthconnect-export)（MIT），在原作者的基礎上修正資料對應錯誤並加入新功能。

## 下載

到 [Releases](https://github.com/curboo/healthconnect-export/releases) 下載最新的 `HCExport-curbo.apk`。

- App 名稱：**HC Export curbo**
- 套件名稱：`com.healthconnect.export.curbo`（可與原版並存）
- 需要 Android 9（API 28）以上，並已安裝／內建 Health Connect

## 這個版本做了什麼

### 修正
- **睡眠階段對應錯誤**：原版把 Health Connect 的階段常數對錯（淺睡被標成 REM、深睡被標成「離床」）。已依官方常數修正：1 清醒、2 睡眠、3 離床、4 淺睡、5 深睡、6 REM、7 床上清醒。
- **運動類型對應錯誤**：改用 Health Connect 正式常數，羽球、步行、重訓等不再被標錯。

### 新增
- **睡眠時段**：`sleep.sessions[]` 輸出每段睡眠的開始／結束時間、實際睡著分鐘、清醒分鐘與入睡潛伏期，可以直接判斷幾點睡、幾點起床。
- **身體組成**：體脂、肌肉量、體水分、骨量、基礎代謝率（BMR）、身高。
- **讀取歷史資料**：加入 `READ_HEALTH_DATA_HISTORY` 權限，可讀取首次授權前 30 天以上的資料（需在 Health Connect 開啟「存取過去資料」）。
- **喝水總量**：`hydration.total_ml`。
- **手機使用時間**：`screen_time` 輸出每天總使用分鐘、解鎖次數、各 App 使用分鐘與 24 小時分布。
- **App 使用時間軸**：`screen_time.sessions[]` 記錄每次前景使用的開始／結束時間（同 App 間隔 < 30 秒合併、< 10 秒的片段捨棄），以及 `unlock_times[]`。
- **繁體中文介面**。

### 原版既有功能
- 每天一份 JSON（`health_YYYY-MM-DD.json`），也可輸出 CSV
- 每日／每週排程匯出，或**每 2 小時送 Webhook**
- Webhook 支援 Bearer Token、失敗重試、一鍵測試
- 開機自動恢復排程、Doze 防睡眠看門狗、離線期間漏掉的日子自動補送
- 選擇資料來源（Samsung Health、Google Fit…）、日期區間、JSON 檢視器、深色模式

## 使用方式

1. 安裝 APK，打開後依提示授予 Health Connect 讀取權限。
2. **手機使用時間**：到 App「設定」開啟「使用情形存取權」（Usage Access）。Android 系統約只保留 7–10 天的使用紀錄。
3. **歷史資料**（選用）：在 Health Connect 權限頁開啟「存取過去資料」。
4. 到「整合」分頁填入 Webhook 網址與 Token，勾選要送的資料類型，按「測試」確認收得到。
5. 到「排程」分頁開啟「每 2 小時傳送」。建議把 App 設為電池「不受限制」。

## 輸出格式（節錄）

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

實際欄位依手錶與 App 寫入 Health Connect 的資料而定；沒有的欄位會是 `null` 或不出現。

## Webhook

| 項目 | 值 |
|---|---|
| 方法 | `POST` |
| Content-Type | `application/json` |
| 驗證 | `Authorization: Bearer <token>`（選用） |
| 逾時 | 15 秒 |
| 重試 | 5xx／網路錯誤時 1 秒後重試 1 次 |

同一天會被送很多次（每 2 小時一次），接收端建議以日期為 key **合併**，新值覆蓋舊值，避免後來的部分資料把先前的完整資料蓋掉。

## 我的資料流程

```
Galaxy Watch → Samsung Health → Health Connect ─┐
Android 使用情形存取權 ──────────────────────────┤
                                                ▼
                         HC Export curbo（每 2 小時）
                                                │ POST JSON
                                                ▼
                     Cloudflare Worker → KV（每日一份，合併寫入）
                                                │
                       ┌────────────────────────┴───────────┐
                       ▼                                    ▼
               私人儀表板（PWA）                 每日自動評分／Google Drive 備份
```

## 自行建置

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 單元測試
./gradlew testDebugUnitTest
```

推送 `v*` 標籤（例如 `v1.14-curbo.0`）時，GitHub Actions 會自動建置簽章後的 APK 並發佈到 Releases。

```bash
git tag v1.14-curbo.1
git push origin v1.14-curbo.1
```

## 版本紀錄

| 版本 | 內容 |
|---|---|
| v1.14-curbo.0 | App 使用時間軸 `screen_time.sessions[]`、解鎖時間 |
| v1.13-curbo.0 | 手機使用時間 `screen_time`、喝水 `hydration.total_ml` |
| v1.12-curbo.0 | 讀取歷史資料權限 |
| v1.11-curbo.0 | 繁體中文介面、身體組成與 BMR、固定簽章金鑰 |
| v1.10-curbo.x | 修正睡眠階段與運動類型對應、睡眠時段時間 |

原版完整變更見 [CHANGELOG.md](CHANGELOG.md)。

## 隱私

所有資料只會存在你的手機，或送到**你自己設定**的 Webhook／Google Drive。App 不會把資料傳給任何第三方。

## 授權

MIT License。原始程式 © kas-cor，修改部分 © curbo（劉宏昱）。詳見 [LICENSE](LICENSE)。
