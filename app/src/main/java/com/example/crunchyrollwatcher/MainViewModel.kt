package com.example.crunchyrollwatcher

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(context: Context) : ViewModel() {

    private val rssRepository = RssRepository()
    private val savedTitlesRepository: SavedTitlesRepository = SavedTitlesRepositoryImpl(context)

    private val _rssEpisodes = MutableLiveData<List<CrunchyrollEpisode>>()
    val rssEpisodes: LiveData<List<CrunchyrollEpisode>> = _rssEpisodes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _savedTitles = MutableLiveData<List<SavedTitle>>()
    val savedTitles: LiveData<List<SavedTitle>> = _savedTitles

    private val _currentFilter = MutableLiveData<RssFilter>()
    val currentFilter: LiveData<RssFilter> = _currentFilter

    init {
        _rssEpisodes.value = emptyList()
        _isLoading.value = false
        _errorMessage.value = ""
        _currentFilter.value = RssFilter()
        loadSavedTitles()
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

    // Favorites/Saved Titles functionality
    fun loadSavedTitles() {
        viewModelScope.launch {
            try {
                val titles = savedTitlesRepository.getSavedTitles()
                _savedTitles.value = titles
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load saved titles: ${e.message}"
            }
        }
    }

    fun saveTitle(episode: CrunchyrollEpisode) {
        viewModelScope.launch {
            try {
                val savedTitle = SavedTitle.fromCrunchyrollEpisode(episode)
                val success = savedTitlesRepository.saveTitleToList(savedTitle)

                if (success) {
                    loadSavedTitles() // Refresh the list
                } else {
                    _errorMessage.value = "Failed to save title"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save title: ${e.message}"
            }
        }
    }

    fun removeSavedTitle(titleId: String) {
        viewModelScope.launch {
            try {
                val success = savedTitlesRepository.removeTitleFromList(titleId)

                if (success) {
                    loadSavedTitles() // Refresh the list
                } else {
                    _errorMessage.value = "Failed to remove title"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove title: ${e.message}"
            }
        }
    }

    fun isTitleSaved(seriesTitle: String): Boolean {
        val titleId = seriesTitle.lowercase().replace(Regex("[^a-z0-9]"), "_")
        return savedTitlesRepository.isTitleSaved(titleId)
    }

    fun updateEpisodeProgress(episode: CrunchyrollEpisode) {
        viewModelScope.launch {
            try {
                val titleId = episode.seriesTitle.lowercase().replace(Regex("[^a-z0-9]"), "_")
                if (savedTitlesRepository.isTitleSaved(titleId)) {
                    savedTitlesRepository.updateEpisodeCount(titleId, episode.episodeNumber)
                    loadSavedTitles() // Refresh the list
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update episode progress: ${e.message}"
            }
        }
    }

}
