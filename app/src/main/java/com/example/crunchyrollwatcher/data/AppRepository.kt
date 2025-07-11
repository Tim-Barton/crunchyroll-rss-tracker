package com.example.crunchyrollwatcher.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository class that manages app data and provides a clean API for data operations.
 * This demonstrates the Repository pattern for better architecture and separation of concerns.
 */
class AppRepository {

    /**
     * Simulates fetching user profile data from a remote source or local database
     */
    suspend fun getUserProfile(): UserProfile {
        // Simulate network delay
        delay(1000)
        return UserProfile(
            name = "John Doe",
            jobTitle = "Android Developer",
            projectsCount = 12,
            experienceYears = 3,
            primarySkill = "Kotlin"
        )
    }

    /**
     * Provides a flow of activities for reactive updates
     */
    fun getActivitiesFlow(): Flow<List<AppActivity>> = flow {
        // Simulate loading
        delay(500)
        emit(getActivities())

        // Simulate periodic updates
        while (true) {
            delay(30000) // Update every 30 seconds
            emit(getActivities())
        }
    }

    /**
     * Gets the current list of activities
     */
    fun getActivities(): List<AppActivity> {
        return listOf(
            AppActivity(
                title = "Profile Updated",
                description = "Changed profile information",
                time = "2 min ago",
                icon = Icons.Default.Person
            ),
            AppActivity(
                title = "New Message",
                description = "Received a new notification",
                time = "5 min ago",
                icon = Icons.Default.Email
            ),
            AppActivity(
                title = "App Launched",
                description = "Started the application",
                time = "10 min ago",
                icon = Icons.Default.PlayArrow
            ),
            AppActivity(
                title = "Settings Changed",
                description = "Modified app preferences",
                time = "1 hour ago",
                icon = Icons.Default.Settings
            ),
            AppActivity(
                title = "Data Synced",
                description = "Synchronized with server",
                time = "2 hours ago",
                icon = Icons.Default.Refresh
            )
        )
    }

    /**
     * Simulates saving user preferences
     */
    suspend fun saveUserPreferences(preferences: UserPreferences): Boolean {
        delay(200) // Simulate save operation
        return true
    }

    /**
     * Gets user preferences
     */
    suspend fun getUserPreferences(): UserPreferences {
        delay(100) // Simulate fetch operation
        return UserPreferences(
            darkModeEnabled = false,
            notificationsEnabled = true
        )
    }

    /**
     * Adds a new activity to the list
     */
    suspend fun addActivity(activity: AppActivity): Boolean {
        delay(100)
        // In a real app, this would save to database or send to server
        return true
    }

    /**
     * Simulates fetching app information
     */
    fun getAppInfo(): AppInfo {
        return AppInfo(
            name = "Basic Android App",
            version = "1.0.0",
            description = "A demonstration of basic Android app features using Jetpack Compose and Kotlin."
        )
    }
}

/**
 * Data class representing an app activity/event
 */
data class AppActivity(
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector
)

/**
 * Data class representing user profile information
 */
data class UserProfile(
    val name: String,
    val jobTitle: String,
    val projectsCount: Int,
    val experienceYears: Int,
    val primarySkill: String
)

/**
 * Data class representing user preferences
 */
data class UserPreferences(
    val darkModeEnabled: Boolean,
    val notificationsEnabled: Boolean
)

/**
 * Data class representing app information
 */
data class AppInfo(
    val name: String,
    val version: String,
    val description: String
)
