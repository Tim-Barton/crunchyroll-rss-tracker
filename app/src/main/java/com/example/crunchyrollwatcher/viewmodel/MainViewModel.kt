package com.example.crunchyrollwatcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crunchyrollwatcher.data.AppActivity
import com.example.crunchyrollwatcher.data.AppRepository
import com.example.crunchyrollwatcher.data.UserPreferences
import com.example.crunchyrollwatcher.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the main app functionality.
 * Manages UI state and handles business logic using the Repository pattern.
 */
class MainViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    // UI State for activities
    private val _activities = MutableStateFlow<List<AppActivity>>(emptyList())
    val activities: StateFlow<List<AppActivity>> = _activities.asStateFlow()

    // UI State for user profile
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // UI State for user preferences
    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()

    // UI State for loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // UI State for counter (example of local state management)
    private val _counterValue = MutableStateFlow(0)
    val counterValue: StateFlow<Int> = _counterValue.asStateFlow()

    // UI State for selected tab
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        loadInitialData()
    }

    /**
     * Loads initial data when ViewModel is created
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Load activities
                _activities.value = repository.getActivities()

                // Load user profile
                _userProfile.value = repository.getUserProfile()

                // Load user preferences
                _userPreferences.value = repository.getUserPreferences()

            } catch (e: Exception) {
                // Handle error (in a real app, you might want to emit an error state)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refreshes activities data
     */
    fun refreshActivities() {
        viewModelScope.launch {
            try {
                _activities.value = repository.getActivities()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Updates the counter value
     */
    fun updateCounter(newValue: Int) {
        _counterValue.value = newValue
    }

    /**
     * Increments the counter
     */
    fun incrementCounter() {
        _counterValue.value = _counterValue.value + 1
    }

    /**
     * Decrements the counter
     */
    fun decrementCounter() {
        if (_counterValue.value > 0) {
            _counterValue.value = _counterValue.value - 1
        }
    }

    /**
     * Resets the counter to zero
     */
    fun resetCounter() {
        _counterValue.value = 0
    }

    /**
     * Updates the selected tab
     */
    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    /**
     * Updates user preferences
     */
    fun updateUserPreferences(preferences: UserPreferences) {
        viewModelScope.launch {
            try {
                val success = repository.saveUserPreferences(preferences)
                if (success) {
                    _userPreferences.value = preferences
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Toggles dark mode preference
     */
    fun toggleDarkMode() {
        val currentPrefs = _userPreferences.value
        if (currentPrefs != null) {
            val newPrefs = currentPrefs.copy(darkModeEnabled = !currentPrefs.darkModeEnabled)
            updateUserPreferences(newPrefs)
        }
    }

    /**
     * Toggles notifications preference
     */
    fun toggleNotifications() {
        val currentPrefs = _userPreferences.value
        if (currentPrefs != null) {
            val newPrefs = currentPrefs.copy(notificationsEnabled = !currentPrefs.notificationsEnabled)
            updateUserPreferences(newPrefs)
        }
    }

    /**
     * Adds a new activity
     */
    fun addActivity(activity: AppActivity) {
        viewModelScope.launch {
            try {
                val success = repository.addActivity(activity)
                if (success) {
                    // Refresh the activities list
                    refreshActivities()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Gets app information
     */
    fun getAppInfo() = repository.getAppInfo()

    /**
     * Simulates a user action that might take time
     */
    fun performLongRunningTask() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulate some work
                kotlinx.coroutines.delay(2000)
                refreshActivities()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
