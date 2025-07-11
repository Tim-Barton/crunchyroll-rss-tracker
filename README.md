# Basic Android App

A comprehensive Android application built with Kotlin and Jetpack Compose, demonstrating modern Android development practices and architecture patterns.

## Features

- **Modern UI**: Built with Jetpack Compose for declarative UI development
- **Material Design 3**: Implements the latest Material Design guidelines
- **Bottom Navigation**: Three main sections - Home, Profile, and Settings
- **Interactive Components**: Counter functionality, toggles, and buttons
- **Data Management**: Repository pattern for clean architecture
- **State Management**: ViewModel with StateFlow for reactive UI updates
- **Unit Testing**: Comprehensive test coverage for business logic

## Screenshots

The app includes three main screens:

### Home Screen
- Welcome card with app introduction
- Interactive counter with increment/decrement functionality
- Recent activities list with icons and timestamps

### Profile Screen
- User profile information display
- Statistics cards (Projects, Experience, Skills)
- Profile editing functionality

### Settings Screen
- Dark mode toggle
- Notifications preferences
- App information and version details

## Architecture

This app follows the **MVVM (Model-View-ViewModel)** architecture pattern:

- **Model**: Data classes and Repository for data management
- **View**: Jetpack Compose UI components
- **ViewModel**: State management and business logic

### Key Components

- `MainActivity.kt` - Main activity with Compose UI
- `MainViewModel.kt` - ViewModel for state management
- `AppRepository.kt` - Data repository following Repository pattern
- Unit tests for business logic validation

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **State Management**: StateFlow and Compose State
- **Material Design**: Material 3 components
- **Testing**: JUnit for unit tests
- **Build System**: Gradle with Kotlin DSL

## Dependencies

- `androidx.compose.bom` - Compose Bill of Materials
- `androidx.compose.material3` - Material 3 components
- `androidx.lifecycle.viewmodel` - ViewModel architecture component
- `androidx.activity.compose` - Activity integration with Compose
- `androidx.core.ktx` - Android KTX extensions

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK API level 24 or higher
- Kotlin 1.8.0 or later

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd CrunchyrollWatcher
   ```

2. Open the project in Android Studio

3. Build and run the project:
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

### Running Tests

Execute unit tests with:
```bash
./gradlew test
```

## Code Structure

```
app/src/main/java/com/example/crunchyrollwatcher/
├── MainActivity.kt                 # Main activity with UI components
├── data/
│   └── AppRepository.kt           # Data repository and models
├── viewmodel/
│   └── MainViewModel.kt           # ViewModel for state management
└── ui/theme/                      # Theme and styling
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt

app/src/test/java/com/example/crunchyrollwatcher/
└── ExampleUnitTest.kt             # Unit tests
```

## Key Features Demonstrated

1. **Jetpack Compose UI**
   - Declarative UI development
   - Composable functions
   - State management with `remember` and `mutableStateOf`

2. **Material Design 3**
   - Modern design system
   - Adaptive colors and theming
   - Consistent component usage

3. **Architecture Patterns**
   - MVVM architecture
   - Repository pattern
   - Separation of concerns

4. **State Management**
   - ViewModel with StateFlow
   - Reactive UI updates
   - Proper lifecycle management

5. **Navigation**
   - Bottom navigation bar
   - Tab-based navigation
   - State preservation

## Learning Objectives

This app serves as a learning resource for:

- Modern Android development with Kotlin
- Jetpack Compose fundamentals
- Architecture patterns in Android
- State management best practices
- Unit testing in Android projects
- Material Design implementation

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Write/update tests
5. Submit a pull request

## License

This project is for educational purposes and demonstrates Android development best practices.

## Contact

For questions or suggestions about this project, please open an issue in the repository.