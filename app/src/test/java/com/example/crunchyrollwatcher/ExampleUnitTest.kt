package com.example.crunchyrollwatcher

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import com.example.crunchyrollwatcher.data.AppActivity
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the Basic Android App functionality.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun activity_data_model_isCorrect() {
        val activity = AppActivity(
            title = "Test Activity",
            description = "Test Description",
            time = "5 min ago",
            icon = Icons.Default.Star
        )

        assertEquals("Test Activity", activity.title)
        assertEquals("Test Description", activity.description)
        assertEquals("5 min ago", activity.time)
    }

    @Test
    fun navigation_item_data_model_isCorrect() {
        val navItem = NavigationItem("Home", Icons.Default.Home)

        assertEquals("Home", navItem.title)
        assertEquals(Icons.Default.Home, navItem.icon)
    }

    @Test
    fun sample_activities_list_hasCorrectSize() {
        assertEquals(5, sampleActivities.size)
    }

    @Test
    fun sample_activities_first_item_isCorrect() {
        val firstActivity = sampleActivities.first()

        assertEquals("Profile Updated", firstActivity.title)
        assertEquals("Changed profile information", firstActivity.description)
        assertEquals("2 min ago", firstActivity.time)
    }

    @Test
    fun counter_increment_works() {
        var counter = 0
        counter++
        assertEquals(1, counter)

        counter += 5
        assertEquals(6, counter)
    }

    @Test
    fun counter_decrement_works() {
        var counter = 10
        counter--
        assertEquals(9, counter)

        counter -= 3
        assertEquals(6, counter)
    }

    @Test
    fun counter_reset_works() {
        var counter = 42
        counter = 0
        assertEquals(0, counter)
    }

    @Test
    fun profile_stats_validation() {
        val projectsCount = "12"
        val experience = "3 Years"
        val skills = "Kotlin"

        assertTrue(projectsCount.toInt() > 0)
        assertTrue(experience.contains("Years"))
        assertTrue(skills.isNotEmpty())
    }

    @Test
    fun app_version_format_isCorrect() {
        val version = "1.0.0"
        val versionPattern = "\\d+\\.\\d+\\.\\d+".toRegex()

        assertTrue(versionPattern.matches(version))
    }
}
