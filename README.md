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

# Crunchyroll RSS Tracker

A modern Android application built with Kotlin for tracking the latest Crunchyroll episodes via RSS feeds and managing your anime watchlist. This app provides a clean, intuitive tabbed interface to browse the latest episode releases and search for specific anime titles.

## Features

- **RSS Feed Reader**: Real-time access to Crunchyroll's latest episode releases
- **Episode Tracking**: Mark episodes as watched/unwatched
- **Smart Filtering**: Filter episodes by All, New Episodes, Dubs, or Subs
- **Episode Search**: Search through episodes by title, series, or description
- **Browse Popular Anime**: Discover trending and popular anime titles
- **Search Functionality**: Find specific anime by title, description, or genre
- **Watchlist Management**: Add anime to your personal watchlist
- **Progress Tracking**: Track your viewing progress for each anime
- **Material Design UI**: Clean, modern tabbed interface following Material Design principles
- **Share & Open**: Share episodes or open them directly in your browser
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
- **HTTP Client**: OkHttp3 for RSS feed fetching
- **XML Parsing**: Built-in XML Pull Parser for RSS parsing
- **Image Loading**: Glide for episode thumbnails
- **Navigation**: ViewPager2 with TabLayout
- **Build System**: Gradle with Kotlin DSL
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/crunchyrollwatcher/
│   │   ├── MainActivity.kt          # Main activity with tabs
│   │   ├── MainViewModel.kt         # ViewModel for business logic
│   │   ├── MainPagerAdapter.kt      # ViewPager adapter for tabs
│   │   ├── RssFeedFragment.kt       # RSS feed tab fragment
│   │   ├── SearchFragment.kt        # Search tab fragment
│   │   ├── RssModels.kt             # RSS data models
│   │   ├── RssRepository.kt         # RSS feed fetching and parsing
│   │   ├── RssEpisodeAdapter.kt     # RecyclerView adapter for episodes
│   │   ├── Anime.kt                 # Data model for anime
│   │   └── AnimeAdapter.kt          # RecyclerView adapter for anime
│   ├── res/
│   │   ├── layout/                  # XML layout files
│   │   │   ├── activity_main.xml    # Main tabbed layout
│   │   │   ├── fragment_rss_feed.xml# RSS feed tab layout
│   │   │   ├── fragment_search.xml  # Search tab layout
│   │   │   ├── item_episode.xml     # Episode list item
│   │   │   └── item_anime.xml       # Anime list item
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

### RSS Feed Tab
- **Live Episode Feed**: Fetches latest episodes from Crunchyroll's RSS feed
- **Episode Cards**: Display series title, episode title, thumbnail, description, and publish date
- **Smart Filtering**: Filter by All, New Episodes, Dubs, or Subs using chip filters
- **Episode Search**: Real-time search through episode titles, series names, and descriptions
- **Watch Status**: Mark episodes as watched/unwatched with visual indicators
- **Episode Actions**: Open in browser, mark as watched, or share episode links
- **Auto-refresh**: Pull-to-refresh functionality and auto-refresh on app resume
- **Error Handling**: Graceful error handling with retry options

### Search Tab
- **Anime Discovery**: Browse popular anime titles
- **Search Functionality**: Find specific anime by title, description, or genre
- **Anime Cards**: Show title, rating, description, genres, and episode count
- **Watchlist Management**: Add anime to personal watchlist
- **Progress Tracking**: Track viewing progress for each anime

### RSS Feed Details
- **Data Source**: Crunchyroll official RSS feed (https://www.crunchyroll.com/rss)
- **Episode Information**: Extracts series title, episode number, description, and thumbnail
- **Language Support**: Automatically detects and filters dubs vs subs
- **Real-time Updates**: Fetches latest episodes on demand
- **Offline Handling**: Graceful handling of network issues

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

### RSS Implementation
The app includes a complete RSS feed reader implementation:
- **XML Parsing**: Custom XML parser for Crunchyroll's RSS format
- **Data Models**: Structured models for RSS feed, channel, and episode data
- **Image Loading**: Glide integration for episode thumbnails
- **Error Handling**: Comprehensive error handling and user feedback
- **Caching**: In-memory episode list caching with refresh capabilities

### Mock Data
The search tab uses mock data for anime information. In a production version, this would be replaced with:
- REST API calls to anime databases (MyAnimeList, AniList, etc.)
- Local database for offline storage and watchlist persistence

### Future Enhancements
- **Local Database**: Room database for offline episode storage and watch history
- **Push Notifications**: Episode release notifications for followed series
- **User Profiles**: Personal accounts with cloud sync for watch history
- **Advanced Filtering**: Filter by specific series, release date, or language
- **Episode Downloads**: Offline viewing capabilities
- **Detailed Episode Pages**: Full episode information, reviews, and recommendations
- **Social Features**: Share watchlists and episode recommendations
- **API Integration**: Connect to real anime databases for the search tab
- **Dark Mode**: Theme support for different viewing preferences
- **Widget Support**: Home screen widget showing latest episodes

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

**Note**: This app reads from Crunchyroll's public RSS feed for educational purposes. It is not affiliated with Crunchyroll or any anime streaming service. The search functionality uses mock data for demonstration.
