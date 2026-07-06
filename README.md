
# TextWall

**TextWall** is a lightweight Android application that dynamically renders time-aware, text-based wallpapers. It schedules specific motivational quotes and digital wellbeing reminders based on the time of day, utilizing minimal battery and storage by generating bitmaps entirely in-memory.

## Download

[![Download APK](https://img.shields.io/badge/Download-APK-green?style=for-the-badge&logo=android)](https://github.com/rizalfadlil/TextWall/releases/latest/download/app-release.apk)

## Features

1. Dynamic time-based wallpaper quotes.
2. Material 3 Expressive UI.
3. Screen-off accessibility feature.
4. System diagnostic checks for permissions.

## Screenshots

| Main Screen | Settings Screen | Example |
|---|---|---|
| ![Main Screen](images/main.png) | ![Settings Screen](images/settings.png) | ![Example](images/example.png) |

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Background Tasks**: WorkManager
- **Data Persistence**: DataStore (Preferences)
- **Serialization**: Kotlinx Serialization (JSON)
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 15)

## Architecture

### Core Components

- **MainActivity.kt**: Main activity hosting the Compose UI and managing app state
- **WallpaperTriggerService.kt**: Foreground service that listens for screen unlock events
- **ScreenOffAccessibilityService.kt**: Accessibility service for screen lock functionality
- **WallpaperWorker.kt**: WorkManager worker that handles wallpaper updates
- **TextProvider.kt**: Provides time-based motivational text content
- **SettingsRepository.kt**: Manages app settings using DataStore
- **SettingsScreen.kt**: Compose UI for app customization
- **SystemDiagnosticsCard.kt**: Displays system permission status

### Background Services

1. **WallpaperTriggerService**: Monitors device unlock events and triggers wallpaper updates
2. **ScreenOffAccessibilityService**: Provides screen lock functionality
3. **WallpaperTriggerReceiver**: Handles boot completed events to restart services

## Permissions Required

The app requires the following permissions:

- `SET_WALLPAPER` - To change the lockscreen wallpaper
- `RECEIVE_BOOT_COMPLETED` - To restart services after device reboot
- `FOREGROUND_SERVICE` - To run background monitoring service
- `FOREGROUND_SERVICE_SPECIAL_USE` - Special use foreground service for screen detection
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` - To prevent battery optimization from killing background services
- `POST_NOTIFICATIONS` - To display notifications (Android 13+)

## Setup Instructions

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11 or higher
- Android SDK with API level 36

### Building the Project

1. Clone the repository:
```bash
git clone <repository-url>
cd TextWall
```

2. Open the project in Android Studio

3. Sync Gradle files:
```bash
./gradlew sync
```

4. Build the project:
```bash
./gradlew assembleDebug
```

5. Install on device:
```bash
./gradlew installDebug
```

## Usage

### First-Time Setup

1. **Grant Notification Permission** (Android 13+):
   - The app will prompt for notification permission on first launch
   - Required for foreground service notifications

2. **Disable Battery Optimization**:
   - Navigate to battery optimization settings
   - Select "TextWall" and choose "Don't optimize"
   - Ensures background services run reliably

3. **Enable Accessibility Service**:
   - Go to Accessibility settings
   - Find "TextWall" and enable it
   - Required for screen lock functionality

### Main Features

- **Enable/Disable**: Toggle the wallpaper changer on/off
- **Turn Screen Off**: Lock your device directly from the app
- **Settings**: Customize text content and styling preferences
- **Preview**: View how your lockscreen will appear with current settings

### Text Formatting

Use single quotes to highlight specific words in gold:
- Example: `Stay 'focused' and keep 'pushing' forward`
- Highlighted words will appear in bold gold color

## Project Structure

```
app/src/main/java/com/rizalalfadlil/textwall/
├── MainActivity.kt                      # Main activity and UI
├── ScreenOffAccessibilityService.kt    # Accessibility service
├── SettingsRepository.kt               # Settings management
├── SettingsScreen.kt                   # Settings UI
├── SystemDiagnosticsCard.kt            # Permission status UI
├── TextProvider.kt                     # Motivational text provider
├── WallpaperTriggerReceiver.kt         # Boot receiver
├── WallpaperTriggerService.kt          # Foreground service
├── WallpaperWorker.kt                  # WorkManager worker
├── WorkScheduler.kt                    # Work scheduler
├── utility.kt                          # Utility functions
└── ui/theme/                           # Compose theme
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

## Dependencies

Key dependencies include:
- AndroidX Core KTX
- AndroidX Lifecycle Runtime KTX
- AndroidX Activity Compose
- Jetpack Compose BOM
- Material 3
- AndroidX Work Runtime KTX
- AndroidX DataStore Preferences
- Kotlinx Serialization JSON

## License

This project is licensed under the terms specified in the project configuration.

## To Do's

### Core Feature/Important
*all done for now*

### Less Important (when there's no important todo)
*all done for now*

### Future Plan
- Generate new Quotes daily with AI or external APIs

## Known Issues
- `P1`: Wallpaper Eventually changed to default (I Don't Know The cause, is this caused by the App or the Device itself)
- `P2`: Accessibility settings permission reset after clicking restart button inside the app on some phone (won't reset if the app is restarted manually)

### Tested on
|name|details|type|problem|
|---|---|---|---|
|Medium Phone API 36.1|Android 16, x86_64|Virtual|`P1`|
|Infinix X6885|XOS 15 (Android 15), arm64|Physical|`P1`, `P2`|

## Contributing

Contributions, issues, and feature requests are welcome!
Feel free to check the issues page if you want to contribute.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

RizalAlfadlil

https://github.com/rizalfadlil

## Version

Current version: 1.0.1 (versionCode 2)
