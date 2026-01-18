# Geo Silent Mode

An Android MVP application that automates phone behavior based on user-defined geographical zones.

## Features

- **Location-Based Automation**: Define circular geofence zones on a map
- **Silent Mode**: Automatically enables silent mode when entering a zone
- **Do Not Disturb**: Optional DND mode activation
- **SMS Notifications**: Send automated messages when entering zones
- **App Launch**: Open specific apps on zone entry
- **Zone Management**: Create, edit, delete, and toggle zones with persistence
- **Battery Optimized**: Uses system geofencing API, no continuous GPS tracking
- **Free Maps**: Uses OpenStreetMap - no API key required!

## Requirements

- Android 10 (API 29) or higher
- Google Play Services (for geofencing)
- Internet connection (for map tiles)

## Setup

### Build the Project

No API keys needed! Simply open the project in Android Studio and sync Gradle:

```bash
./gradlew build
```

Or build the debug APK:

```bash
./gradlew assembleDebug
```

## Project Structure

```
app/src/main/java/com/geosilent/
├── data/
│   ├── database/        # Room database entities and DAOs
│   └── repository/      # Data repositories
├── di/                  # Hilt dependency injection
├── service/             # Geofencing service and actions
│   └── actions/         # Action executors (silent, DND, SMS, app)
├── ui/
│   ├── navigation/      # Navigation graph
│   ├── screens/         # Compose UI screens
│   ├── theme/           # Material 3 theming
│   └── viewmodels/      # Screen ViewModels
└── utils/               # Utility classes
```

## Permissions

The app requires the following permissions:

| Permission | Purpose |
|------------|---------|
| `INTERNET` | Download OpenStreetMap tiles |
| `ACCESS_FINE_LOCATION` | Detect current location |
| `ACCESS_BACKGROUND_LOCATION` | Geofence detection when app is closed |
| `ACCESS_NOTIFICATION_POLICY` | Enable Silent/DND mode |
| `SEND_SMS` | Send automated SMS messages |
| `RECEIVE_BOOT_COMPLETED` | Re-register geofences after reboot |

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Hilt DI
- **Database**: Room
- **Location**: Google Play Services Geofencing API
- **Maps**: OpenStreetMap (osmdroid) - No API key required!

## License

This project is for personal/educational use.
