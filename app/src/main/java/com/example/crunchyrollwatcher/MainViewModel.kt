package com.example.crunchyrollwatcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val rssRepository = RssRepository()

    private val _animeList = MutableLiveData<List<Anime>>()
    val animeList: LiveData<List<Anime>> = _animeList

    private val _rssEpisodes = MutableLiveData<List<CrunchyrollEpisode>>()
    val rssEpisodes: LiveData<List<CrunchyrollEpisode>> = _rssEpisodes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _watchlist = MutableLiveData<List<Anime>>()
    val watchlist: LiveData<List<Anime>> = _watchlist

    private val _currentFilter = MutableLiveData<RssFilter>()
    val currentFilter: LiveData<RssFilter> = _currentFilter

    init {
        _animeList.value = emptyList()
        _rssEpisodes.value = emptyList()
        _watchlist.value = emptyList()
        _isLoading.value = false
        _errorMessage.value = ""
        _currentFilter.value = RssFilter()
    }

    fun loadPopularAnime() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = ""

                // Simulate API call delay
                delay(1000)

                // Mock data for popular anime
                val popularAnime = getMockPopularAnime()
                _animeList.value = popularAnime

            } catch (e: Exception) {
                _errorMessage.value = "Failed to load popular anime: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRssEpisodes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = ""

                val result = rssRepository.fetchCrunchyrollRss()
                result.fold(
                    onSuccess = { episodes ->
                        val filteredEpisodes = rssRepository.filterEpisodes(episodes, _currentFilter.value ?: RssFilter())
                        val sortedEpisodes = rssRepository.sortEpisodesByDate(filteredEpisodes)
                        _rssEpisodes.value = sortedEpisodes
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Failed to load RSS episodes: ${exception.message}"
                    }
                )

            } catch (e: Exception) {
                _errorMessage.value = "Failed to load RSS episodes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyFilter(filter: RssFilter) {
        _currentFilter.value = filter
        val currentEpisodes = _rssEpisodes.value ?: return
        val filteredEpisodes = rssRepository.filterEpisodes(currentEpisodes, filter)
        val sortedEpisodes = rssRepository.sortEpisodesByDate(filteredEpisodes)
        _rssEpisodes.value = sortedEpisodes
    }

    fun refreshRssFeed() {
        loadRssEpisodes()
    }

    fun markEpisodeAsWatched(episodeId: String, watched: Boolean) {
        val currentEpisodes = _rssEpisodes.value?.toMutableList() ?: return
        val episodeIndex = currentEpisodes.indexOfFirst { it.id == episodeId }

        if (episodeIndex != -1) {
            val updatedEpisode = currentEpisodes[episodeIndex].copy(isWatched = watched)
            currentEpisodes[episodeIndex] = updatedEpisode
            _rssEpisodes.value = currentEpisodes
        }
    }

    fun searchEpisodes(query: String) {
        val allEpisodes = _rssEpisodes.value ?: return
        val searchResults = if (query.isBlank()) {
            allEpisodes
        } else {
            allEpisodes.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.seriesTitle.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
        _rssEpisodes.value = searchResults
    }

    fun searchAnime(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = ""

                // Simulate API call delay
                delay(800)

                // Mock search results
                val searchResults = getMockSearchResults(query)
                _animeList.value = searchResults

            } catch (e: Exception) {
                _errorMessage.value = "Search failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToWatchlist(anime: Anime) {
        val currentWatchlist = _watchlist.value?.toMutableList() ?: mutableListOf()
        if (!currentWatchlist.any { it.id == anime.id }) {
            currentWatchlist.add(anime)
            _watchlist.value = currentWatchlist
        }
    }

    fun removeFromWatchlist(animeId: String) {
        val currentWatchlist = _watchlist.value?.toMutableList() ?: mutableListOf()
        currentWatchlist.removeAll { it.id == animeId }
        _watchlist.value = currentWatchlist
    }

    fun updateWatchProgress(animeId: String, watchedEpisodes: Int) {
        val currentList = _animeList.value?.toMutableList() ?: mutableListOf()
        val animeIndex = currentList.indexOfFirst { it.id == animeId }

        if (animeIndex != -1) {
            val updatedAnime = currentList[animeIndex].copy(
                watchedEpisodes = watchedEpisodes,
                isWatched = watchedEpisodes > 0
            )
            currentList[animeIndex] = updatedAnime
            _animeList.value = currentList
        }

        // Also update in watchlist if present
        val currentWatchlist = _watchlist.value?.toMutableList() ?: mutableListOf()
        val watchlistIndex = currentWatchlist.indexOfFirst { it.id == animeId }

        if (watchlistIndex != -1) {
            val updatedAnime = currentWatchlist[watchlistIndex].copy(
                watchedEpisodes = watchedEpisodes,
                isWatched = watchedEpisodes > 0
            )
            currentWatchlist[watchlistIndex] = updatedAnime
            _watchlist.value = currentWatchlist
        }
    }

    private fun getMockPopularAnime(): List<Anime> {
        return listOf(
            Anime(
                id = "1",
                title = "Attack on Titan",
                description = "Humanity fights for survival against giant humanoid Titans.",
                imageUrl = "https://example.com/aot.jpg",
                rating = 9.0,
                episodeCount = 87,
                status = "Completed",
                genres = listOf("Action", "Drama", "Fantasy"),
                releaseYear = 2013
            ),
            Anime(
                id = "2",
                title = "Demon Slayer",
                description = "A young boy becomes a demon slayer to save his sister.",
                imageUrl = "https://example.com/demonslayer.jpg",
                rating = 8.7,
                episodeCount = 44,
                status = "Ongoing",
                genres = listOf("Action", "Supernatural", "Historical"),
                releaseYear = 2019
            ),
            Anime(
                id = "3",
                title = "My Hero Academia",
                description = "A world where people with superpowers are the norm.",
                imageUrl = "https://example.com/mha.jpg",
                rating = 8.5,
                episodeCount = 138,
                status = "Ongoing",
                genres = listOf("Action", "School", "Superhero"),
                releaseYear = 2016
            ),
            Anime(
                id = "4",
                title = "One Piece",
                description = "A pirate's quest to find the ultimate treasure.",
                imageUrl = "https://example.com/onepiece.jpg",
                rating = 9.2,
                episodeCount = 1000,
                status = "Ongoing",
                genres = listOf("Action", "Adventure", "Comedy"),
                releaseYear = 1999
            ),
            Anime(
                id = "5",
                title = "Jujutsu Kaisen",
                description = "Students battle cursed spirits at Tokyo Jujutsu High.",
                imageUrl = "https://example.com/jjk.jpg",
                rating = 8.8,
                episodeCount = 24,
                status = "Ongoing",
                genres = listOf("Action", "Supernatural", "School"),
                releaseYear = 2020
            )
        )
    }

    private fun getMockSearchResults(query: String): List<Anime> {
        val allAnime = getMockPopularAnime()
        return allAnime.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.genres.any { genre -> genre.contains(query, ignoreCase = true) }
        }
    }
}
