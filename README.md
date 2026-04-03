# PicViewApp

An Android app for browsing images in folders with sorting options.

## Features

1. **Browse any folder** - Open any folder on your phone and view all images inside
2. **Multiple sorting options** - Sort images by name, date, or size (ascending/descending)
3. **Swipe gallery** - Swipe horizontally through images in full-screen viewer
4. **Recent folders** - Home screen shows recently accessed folders
5. **Search folders** - Search for folders by name

## Permissions Required

- **Storage access** - To read images from folders
- **MANAGE_EXTERNAL_STORAGE** - For full access on Android 11+

## Building the Project

### Using Android Studio

1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the `pic_view_app` folder
4. Wait for Gradle sync to complete
5. Run the app on a device or emulator

### Using Command Line

1. Ensure JDK 17+ and Android SDK are installed
2. Set `ANDROID_HOME` environment variable
3. Run:
   ```
   cd pic_view_app
   ./gradlew assembleDebug
   ```

## Project Structure

```
pic_view_app/
├── app/
│   ├── src/main/
│   │   ├── java/com/picviewapp/
│   │   │   ├── data/           # Data layer
│   │   │   │   ├── model/      # Data models
│   │   │   │   └── repository/ # Repositories
│   │   │   ├── di/             # Dependency injection
│   │   │   ├── domain/         # Domain layer
│   │   │   │   └── usecase/    # Use cases
│   │   │   └── ui/             # UI layer
│   │   │       ├── home/       # Home screen
│   │   │       ├── browser/    # Folder browser
│   │   │       ├── viewer/     # Image viewer
│   │   │       └── search/     # Search screen
│   │   └── res/                # Resources
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## Tech Stack

- Kotlin
- Jetpack Compose
- Hilt (Dependency Injection)
- Coil (Image Loading)
- DataStore (Preferences)
- Material 3
