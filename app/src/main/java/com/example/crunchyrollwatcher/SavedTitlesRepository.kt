package com.example.crunchyrollwatcher

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SavedTitlesRepositoryImpl(context: Context) : SavedTitlesRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "saved_titles_prefs"
        private const val KEY_SAVED_TITLES = "saved_titles"
    }

    override fun getSavedTitles(): List<SavedTitle> {
        val json = sharedPreferences.getString(KEY_SAVED_TITLES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<SavedTitle>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun saveTitleToList(savedTitle: SavedTitle): Boolean {
        return try {
            val currentTitles = getSavedTitles().toMutableList()

            // Check if title already exists
            val existingIndex = currentTitles.indexOfFirst { it.id == savedTitle.id }
            if (existingIndex != -1) {
                // Update existing title with new episode info
                val existingTitle = currentTitles[existingIndex]
                val updatedTitle = existingTitle.copy(
                    episodeCount = maxOf(existingTitle.episodeCount, savedTitle.episodeCount),
                    lastEpisodeSeen = savedTitle.lastEpisodeSeen ?: existingTitle.lastEpisodeSeen,
                    imageUrl = savedTitle.imageUrl ?: existingTitle.imageUrl,
                    description = savedTitle.description ?: existingTitle.description
                )
                currentTitles[existingIndex] = updatedTitle
            } else {
                // Add new title
                currentTitles.add(savedTitle)
            }

            // Sort by date added (newest first)
            currentTitles.sortByDescending { it.dateAdded }

            val json = gson.toJson(currentTitles)
            sharedPreferences.edit().putString(KEY_SAVED_TITLES, json).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun removeTitleFromList(titleId: String): Boolean {
        return try {
            val currentTitles = getSavedTitles().toMutableList()
            val removed = currentTitles.removeIf { it.id == titleId }

            if (removed) {
                val json = gson.toJson(currentTitles)
                sharedPreferences.edit().putString(KEY_SAVED_TITLES, json).apply()
            }
            removed
        } catch (e: Exception) {
            false
        }
    }

    override fun isTitleSaved(titleId: String): Boolean {
        return getSavedTitles().any { it.id == titleId }
    }

    override fun updateEpisodeCount(titleId: String, episodeNumber: String?): Boolean {
        return try {
            val currentTitles = getSavedTitles().toMutableList()
            val titleIndex = currentTitles.indexOfFirst { it.id == titleId }

            if (titleIndex != -1) {
                val existingTitle = currentTitles[titleIndex]
                val newEpisodeCount = episodeNumber?.toIntOrNull()?.let {
                    maxOf(existingTitle.episodeCount, it)
                } ?: existingTitle.episodeCount + 1

                val updatedTitle = existingTitle.copy(
                    episodeCount = newEpisodeCount,
                    lastEpisodeSeen = episodeNumber ?: existingTitle.lastEpisodeSeen
                )
                currentTitles[titleIndex] = updatedTitle

                val json = gson.toJson(currentTitles)
                sharedPreferences.edit().putString(KEY_SAVED_TITLES, json).apply()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun clearAllSavedTitles(): Boolean {
        return try {
            sharedPreferences.edit().remove(KEY_SAVED_TITLES).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun exportSavedTitles(): String? {
        return try {
            val savedTitles = getSavedTitles()
            gson.toJson(savedTitles)
        } catch (e: Exception) {
            null
        }
    }

    fun importSavedTitles(json: String): Boolean {
        return try {
            val type = object : TypeToken<List<SavedTitle>>() {}.type
            val importedTitles: List<SavedTitle> = gson.fromJson(json, type)

            // Merge with existing titles
            val currentTitles = getSavedTitles().toMutableList()
            importedTitles.forEach { importedTitle ->
                if (currentTitles.none { it.id == importedTitle.id }) {
                    currentTitles.add(importedTitle)
                }
            }

            currentTitles.sortByDescending { it.dateAdded }
            val finalJson = gson.toJson(currentTitles)
            sharedPreferences.edit().putString(KEY_SAVED_TITLES, finalJson).apply()
            true
        } catch (e: Exception) {
            false
        }
    }
}
