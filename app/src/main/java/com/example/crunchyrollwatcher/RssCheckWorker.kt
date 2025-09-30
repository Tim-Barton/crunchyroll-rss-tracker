package com.example.crunchyrollwatcher

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RssCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
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
            // Check if notifications are enabled
            if (!notificationHelper.hasNotificationPermission()) {
                return@withContext Result.success()
            }

            // Get saved titles
            val savedTitles = savedTitlesRepository.getSavedTitles()
            if (savedTitles.isEmpty()) {
                return@withContext Result.success()
            }

            // Fetch RSS feed
            val result = rssRepository.fetchCrunchyrollRss()
            result.fold(
                onSuccess = { episodes ->
                    val newEpisodes = findNewEpisodes(episodes, savedTitles)
                    handleNewEpisodes(newEpisodes)
                    updateLastCheckTimestamp()
                    Result.success()
                },
                onFailure = { exception ->
                    println("RssCheckWorker: Failed to fetch RSS - ${exception.message}")
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            println("RssCheckWorker: Exception during work - ${e.message}")
            Result.failure()
        }
    }

    private fun findNewEpisodes(
        allEpisodes: List<CrunchyrollEpisode>,
        savedTitles: List<SavedTitle>
    ): List<Pair<CrunchyrollEpisode, SavedTitle>> {
        val lastCheckTime = getLastCheckTimestamp()
        val lastKnownEpisodeIds = getLastKnownEpisodeIds()
        val newEpisodesForSavedTitles = mutableListOf<Pair<CrunchyrollEpisode, SavedTitle>>()

        // Create a map for faster lookup
        val savedTitleMap = savedTitles.associateBy {
            it.title.lowercase().trim()
        }

        allEpisodes.forEach { episode ->
            // Check if this episode is for a saved title
            val matchingSavedTitle = savedTitleMap[episode.seriesTitle.lowercase().trim()]

            if (matchingSavedTitle != null) {
                // Check if this is a new episode
                val isNewEpisode = isEpisodeNew(episode, lastCheckTime, lastKnownEpisodeIds)

                if (isNewEpisode) {
                    newEpisodesForSavedTitles.add(episode to matchingSavedTitle)

                    // Update episode progress for saved title
                    savedTitlesRepository.updateEpisodeCount(
                        matchingSavedTitle.id,
                        episode.episodeNumber
                    )
                }
            }
        }

        // Store current episode IDs for next check
        storeCurrentEpisodeIds(allEpisodes.map { it.id })

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

        if (!lastKnownEpisodeIds.contains(episode.id)) {
            return true
        }

        // Try to parse episode publish date
        try {
            val episodeTime = parseEpisodeDate(episode.publishDate)
            if (episodeTime > lastCheckTime) {
                return true
            }
        } catch (e: Exception) {
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
                // No new episodes
                return
            }
            newEpisodes.size == 1 -> {
                // Single new episode - show individual notification
                val (episode, savedTitle) = newEpisodes.first()
                notificationHelper.showNewEpisodeNotification(episode, savedTitle)
            }
            else -> {
                // Multiple new episodes - show summary notification
                notificationHelper.showMultipleEpisodesNotification(newEpisodes)
            }
        }
    }

    private fun getLastCheckTimestamp(): Long {
        return sharedPreferences.getLong(LAST_CHECK_PREF_KEY, 0L)
    }

    private fun updateLastCheckTimestamp() {
        sharedPreferences.edit()
            .putLong(LAST_CHECK_PREF_KEY, System.currentTimeMillis())
            .apply()
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
