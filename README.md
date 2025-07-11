# Author's Note
Used this as a test of where LLM based coding (vibe-coding) was at in a scenario where I was not familiar with the technology.
Everything besides this note (including the remainder of the Readme) was output from Claude
This is test 1 of 2, see the other 'vibe' named branch for the other test.

- Started with an empty project called "Crunchyroll Watcher"
	- Prompted ```create a basic android app in kotlin```
	- Claude made the app including functionality
	- It didn't test the Gradle build, which didn't work as it was incorrectly configured
	- I manually attempted to fix using Android Studio suggestions
	- Then I told Claude to fix it
	- Claude Fixed gradle (eventually)
	- Then I prompted for .gitignore
	- It used 63k/200k allotment from Claude (as provided by [Zed](https://zed.dev))
	- Claude knew what "Crunchyroll" was for, and added some elements in line with an expected usage

# Crunchyroll Watcher

A modern Android application built with Kotlin for tracking and managing your anime watchlist. This app provides a clean, intuitive interface to discover popular anime, search for specific titles, and keep track of your viewing progress.

## Features

- **Browse Popular Anime**: Discover trending and popular anime titles
- **Search Functionality**: Find specific anime by title, description, or genre
- **Watchlist Management**: Add anime to your personal watchlist
- **Progress Tracking**: Track your viewing progress for each anime
- **Material Design UI**: Clean, modern interface following Material Design principles
- **Responsive Layout**: Optimized for various screen sizes

## Screenshots

*Screenshots will be added once the app is running*

## Architecture

This app follows modern Android development practices:

- **MVVM Architecture**: Clean separation of concerns using Model-View-ViewModel pattern
- **LiveData**: Reactive data observation for UI updates
- **Kotlin Coroutines**: Asynchronous programming for smooth user experience
- **RecyclerView**: Efficient list rendering with DiffUtil for performance
- **Material Design Components**: Modern UI components and theming

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Android Views with Material Design Components
- **Architecture**: MVVM with LiveData
- **Async Programming**: Kotlin Coroutines
- **Build System**: Gradle with Kotlin DSL
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/crunchyrollwatcher/
│   │   ├── MainActivity.kt          # Main activity with RecyclerView
│   │   ├── MainViewModel.kt         # ViewModel for business logic
│   │   ├── Anime.kt                 # Data model for anime
│   │   └── AnimeAdapter.kt          # RecyclerView adapter
│   ├── res/
│   │   ├── layout/                  # XML layout files
│   │   ├── values/                  # Colors, strings, themes
│   │   ├── drawable/                # Icons and drawables
│   │   └── mipmap/                  # App icons
│   └── AndroidManifest.xml
├── build.gradle                     # App-level build configuration
└── proguard-rules.pro              # ProGuard configuration
```

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer
- JDK 8 or newer
- Android SDK with API level 24+

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/crunchyroll-watcher.git
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Run the app on an emulator or physical device

### Building

To build the project from command line:

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## Features in Detail

### Home Screen
- Displays a list of popular anime on app launch
- Search bar at the top for finding specific anime
- Each anime card shows title, description, rating, genres, and episode count

### Anime Cards
- **Title and Rating**: Shows anime title with star rating
- **Description**: Brief synopsis of the anime
- **Genres**: Comma-separated list of genres
- **Episode Information**: Total episode count and status
- **Progress Tracking**: Visual progress bar and episode counter for watched anime
- **Action Button**: Add to watchlist or continue watching

### Search Functionality
- Real-time search through anime titles, descriptions, and genres
- Results update as you type
- Clear visual feedback for search states

### Data Management
- Mock data included for development and testing
- Structured data models with helper methods
- Progress tracking with percentage calculations

## Customization

### Colors
The app uses a custom color scheme defined in `colors.xml`:
- Primary: Orange (#FF6B35) - Inspired by Crunchyroll's branding
- Secondary: Dark gray (#23252F)
- Background: White with light gray accents

### Themes
Material Design 3 theming with custom styles for:
- Buttons and cards
- Text styles and typography
- Input fields and progress indicators

## Development Notes

### Mock Data
Currently uses mock data for anime information. In a production version, this would be replaced with:
- REST API calls to anime databases (MyAnimeList, AniList, etc.)
- Local database for offline storage
- Image loading library (Glide, Coil) for poster images

### Future Enhancements
- **API Integration**: Connect to real anime databases
- **User Authentication**: Personal accounts and cloud sync
- **Detailed Anime Pages**: Full information, reviews, and recommendations
- **Offline Support**: Local database with Room
- **Notifications**: Episode release reminders
- **Social Features**: Share watchlists and recommendations

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Material Design for UI/UX guidelines
- Kotlin community for excellent documentation
- Android developers for comprehensive architecture guidance

## Contact

For questions or suggestions, please open an issue on GitHub.

---

**Note**: This is a sample project for demonstration purposes. The app uses mock data and is not affiliated with Crunchyroll or any anime streaming service.
