# Lockscreen Motivation

An Android application that automatically updates your lockscreen wallpaper with motivational quotes and text based on the time of day. Features dynamic styling, real-time preview, and intelligent background scheduling.

## Features

- **Time-Based Motivational Text**: Displays different motivational quotes based on the current hour of the day
- **Dynamic Wallpaper Generation**: Creates personalized lockscreen wallpapers with styled text
- **Real-Time Preview**: Live preview of how your lockscreen will look with current settings
- **Smart Scheduling**: Automatically updates wallpaper when you unlock your device
- **Customizable Styling**: Random font families, weights, and styles for visual variety
- **Text Highlighting**: Support for highlighting words in gold using single quotes (e.g., `'focus'`)
- **Screen Lock Control**: Built-in screen lock functionality via accessibility service
- **Permission Management**: Integrated system diagnostics and permission requests

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
cd MyApplicationq
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
   - Select "Lockscreen Motivation" and choose "Don't optimize"
   - Ensures background services run reliably

3. **Enable Accessibility Service**:
   - Go to Accessibility settings
   - Find "Lockscreen Motivation" and enable it
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
app/src/main/java/com/example/myapplicationq/
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
- Replace AI-Slopped Default Quotes
- Change the day progress range to same as the *morningStart* slider instead of 24 day hour

## Known Issues
- Wallpaper Eventually changed to default (I Don't Know The cause, is this caused by the App or the Device itself)

## Version

Current version: 1.0 (versionCode 1)
