package com.example.crunchyrollwatcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val rssRepository = RssRepository()

    private val _rssEpisodes = MutableLiveData<List<CrunchyrollEpisode>>()
    val rssEpisodes: LiveData<List<CrunchyrollEpisode>> = _rssEpisodes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage



    private val _currentFilter = MutableLiveData<RssFilter>()
    val currentFilter: LiveData<RssFilter> = _currentFilter

    init {
        _rssEpisodes.value = emptyList()
        _isLoading.value = false
        _errorMessage.value = ""
        _currentFilter.value = RssFilter()
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
