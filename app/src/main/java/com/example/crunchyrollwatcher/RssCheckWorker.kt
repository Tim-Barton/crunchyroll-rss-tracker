package com.example.crunchyrollwatcher

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RssCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "RssCheckWorker"
        const val WORK_NAME = "rss_check_work"
        const val LAST_CHECK_PREF_KEY = "last_rss_check_timestamp"
        const val LAST_EPISODE_IDS_KEY = "last_episode_ids"
    }

    private val rssRepository = RssRepository()
    private val savedTitlesRepository: SavedTitlesRepository = SavedTitlesRepositoryImpl(context)
    private val notificationHelper = NotificationHelper(context)
    private val sharedPreferences = context.getSharedPreferences("rss_check_prefs", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== RssCheckWorker started ===")

            // Check if notifications are enabled
            if (!notificationHelper.hasNotificationPermission()) {
                Log.w(TAG, "Notification permission not granted - skipping check")
                return@withContext Result.success()
            }
            Log.d(TAG, "Notification permission: OK")

            // Get saved titles
            val savedTitles = savedTitlesRepository.getSavedTitles()
            Log.d(TAG, "Saved titles count: ${savedTitles.size}")
            if (savedTitles.isEmpty()) {
                Log.w(TAG, "No saved titles - skipping check")
                return@withContext Result.success()
            }
            savedTitles.forEach { title ->
                Log.d(TAG, "Saved title: '${title.title}' (id: ${title.id})")
            }

            // Fetch RSS feed
            Log.d(TAG, "Fetching RSS feed...")
            val result = rssRepository.fetchCrunchyrollRss()
            result.fold(
                onSuccess = { episodes ->
                    Log.d(TAG, "RSS fetch successful - ${episodes.size} episodes found")
                    val newEpisodes = findNewEpisodes(episodes, savedTitles)
                    Log.d(TAG, "New episodes found: ${newEpisodes.size}")
                    handleNewEpisodes(newEpisodes)
                    updateLastCheckTimestamp()
                    Log.d(TAG, "=== RssCheckWorker completed successfully ===")
                    Result.success()
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to fetch RSS - ${exception.message}", exception)
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during work - ${e.message}", e)
            Result.failure()
        }
    }

    private fun findNewEpisodes(
        allEpisodes: List<CrunchyrollEpisode>,
        savedTitles: List<SavedTitle>
    ): List<Pair<CrunchyrollEpisode, SavedTitle>> {
        val lastCheckTime = getLastCheckTimestamp()
        val lastKnownEpisodeIds = getLastKnownEpisodeIds()
        Log.d(TAG, "Last check time: $lastCheckTime (${java.util.Date(lastCheckTime)})")
        Log.d(TAG, "Last known episode IDs count: ${lastKnownEpisodeIds.size}")

        val newEpisodesForSavedTitles = mutableListOf<Pair<CrunchyrollEpisode, SavedTitle>>()

        // Create a map for faster lookup
        val savedTitleMap = savedTitles.associateBy {
            it.title.lowercase().trim()
        }
        Log.d(TAG, "Saved title map keys: ${savedTitleMap.keys}")

        allEpisodes.forEachIndexed { index, episode ->
            val episodeKey = episode.seriesTitle.lowercase().trim()

            // Check if this episode is for a saved title
            val matchingSavedTitle = savedTitleMap[episodeKey]

            if (matchingSavedTitle != null) {
                Log.d(TAG, "Episode #$index matches saved title: '${episode.seriesTitle}' -> '${matchingSavedTitle.title}'")

                // Check if this is a new episode
                val isNewEpisode = isEpisodeNew(episode, lastCheckTime, lastKnownEpisodeIds)
                Log.d(TAG, "  Episode: ${episode.title} | ID: ${episode.id} | IsNew: $isNewEpisode")

                if (isNewEpisode) {
                    Log.i(TAG, "  ✓ NEW EPISODE DETECTED: ${episode.seriesTitle} - ${episode.title}")
                    newEpisodesForSavedTitles.add(episode to matchingSavedTitle)

                    // Update episode progress for saved title
                    savedTitlesRepository.updateEpisodeCount(
                        matchingSavedTitle.id,
                        episode.episodeNumber
                    )
                }
            } else {
                // Log first 5 non-matching episodes for debugging
                if (index < 5) {
                    Log.d(TAG, "Episode #$index no match: '${episode.seriesTitle}' (key: '$episodeKey')")
                }
            }
        }

        // Store current episode IDs for next check
        storeCurrentEpisodeIds(allEpisodes.map { it.id })
        Log.d(TAG, "Stored ${allEpisodes.size} episode IDs for next check")

        return newEpisodesForSavedTitles
    }

    private fun isEpisodeNew(
        episode: CrunchyrollEpisode,
        lastCheckTime: Long,
        lastKnownEpisodeIds: Set<String>
    ): Boolean {
        // Episode is new if:
        // 1. We haven't seen its ID before
        // 2. It was published after our last check (if we can parse the date)

        val idIsNew = !lastKnownEpisodeIds.contains(episode.id)
        if (idIsNew) {
            Log.d(TAG, "    Episode ID is new: ${episode.id}")
            return true
        }

        // Try to parse episode publish date
        try {
            val episodeTime = parseEpisodeDate(episode.publishDate)
            val isAfterLastCheck = episodeTime > lastCheckTime
            Log.d(TAG, "    Episode date: ${java.util.Date(episodeTime)} | After last check: $isAfterLastCheck")
            if (isAfterLastCheck) {
                return true
            }
        } catch (e: Exception) {
            Log.d(TAG, "    Could not parse episode date: ${episode.publishDate}")
            // If we can't parse the date, rely on ID check
        }

        return false
    }

    private fun parseEpisodeDate(dateString: String): Long {
        return try {
            val inputFormat = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.ENGLISH)
            inputFormat.parse(dateString)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun handleNewEpisodes(newEpisodes: List<Pair<CrunchyrollEpisode, SavedTitle>>) {
        when {
            newEpisodes.isEmpty() -> {
                Log.d(TAG, "No new episodes to notify about")
                return
            }
            newEpisodes.size == 1 -> {
                // Single new episode - show individual notification
                val (episode, savedTitle) = newEpisodes.first()
                Log.i(TAG, "Sending single episode notification: ${episode.seriesTitle} - ${episode.title}")
                notificationHelper.showNewEpisodeNotification(episode, savedTitle)
            }
            else -> {
                // Multiple new episodes - show summary notification
                Log.i(TAG, "Sending multiple episodes notification: ${newEpisodes.size} episodes")
                notificationHelper.showMultipleEpisodesNotification(newEpisodes)
            }
        }
    }

    private fun getLastCheckTimestamp(): Long {
        return sharedPreferences.getLong(LAST_CHECK_PREF_KEY, 0L)
    }

    private fun updateLastCheckTimestamp() {
        val timestamp = System.currentTimeMillis()
        sharedPreferences.edit()
            .putLong(LAST_CHECK_PREF_KEY, timestamp)
            .apply()
        Log.d(TAG, "Updated last check timestamp: $timestamp (${java.util.Date(timestamp)})")
    }

    private fun getLastKnownEpisodeIds(): Set<String> {
        val idsString = sharedPreferences.getString(LAST_EPISODE_IDS_KEY, "") ?: ""
        return if (idsString.isEmpty()) {
            emptySet()
        } else {
            idsString.split(",").toSet()
        }
    }

    private fun storeCurrentEpisodeIds(episodeIds: List<String>) {
        val idsString = episodeIds.joinToString(",")
        sharedPreferences.edit()
            .putString(LAST_EPISODE_IDS_KEY, idsString)
            .apply()
    }
}
