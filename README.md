# App Usage Monitor 📊

An Android app that monitors and displays your app usage statistics, sorted from **least to most used** (toggleable), across customizable timeframes.

---

## Features

- **Timeframe selection**: Today, Yesterday, Last 7 Days, Last 30 Days
- **Sort order toggle**: Least → Most Used (default) or Most → Least Used
- **Total screen time** summary card
- **Per-app details**: usage time, launch count, package name, relative usage bar
- **System apps filter**: toggle system apps on/off from the overflow menu
- **Pull-to-refresh** for instant data reload
- **Material Design UI** with clean card-based layout

---

## How to Build the APK

### Option A: Android Studio (Recommended)

1. **Install Android Studio** from https://developer.android.com/studio
2. Open Android Studio → **File → Open** → select the `AppUsageMonitor` folder
3. Wait for Gradle sync to complete
4. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
5. The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### Option B: Command Line

```bash
cd AppUsageMonitor

# On macOS/Linux:
./gradlew assembleDebug

# On Windows:
gradlew.bat assembleDebug
```

The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

---

## Installation

1. Transfer the APK to your Android phone
2. Open the APK and allow installation from unknown sources if prompted
3. On first launch, the app will request **Usage Access Permission**
4. Tap "Grant Permission" → find "App Usage Monitor" in the list → enable it
5. Return to the app — your usage data will load automatically

---

## How It Works

The app uses Android's **UsageStatsManager** API (available on Android 5.0+):

- Queries `UsageStats` for the selected timeframe
- Aggregates foreground time per package
- Resolves app names and icons via `PackageManager`
- Sorts from least to most used by default
- All data stays local on your device — nothing is sent anywhere

---

## Requirements

- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 15 (API 35)
- **Build tools**: Gradle 8.5, AGP 8.2.2, Kotlin 1.9.22

---

## Project Structure

```
AppUsageMonitor/
├── app/src/main/
│   ├── AndroidManifest.xml          # Permissions & activity declaration
│   ├── java/com/appusage/monitor/
│   │   ├── data/
│   │   │   └── AppUsageInfo.kt      # Data models (AppUsageInfo, TimeFrame, SortOrder)
│   │   ├── ui/
│   │   │   ├── MainActivity.kt      # Main screen with permission handling
│   │   │   ├── MainViewModel.kt     # ViewModel for async data loading
│   │   │   └── AppUsageAdapter.kt   # RecyclerView adapter for the list
│   │   └── util/
│   │       └── UsageStatsHelper.kt  # Core logic: queries UsageStatsManager
│   └── res/
│       ├── layout/                  # XML layouts
│       ├── values/                  # Colors, strings, themes
│       ├── drawable/                # Icons, progress bar, backgrounds
│       └── menu/                    # Overflow menu
├── build.gradle                     # Root build config
├── settings.gradle                  # Project settings
└── gradle/                          # Gradle wrapper
```
