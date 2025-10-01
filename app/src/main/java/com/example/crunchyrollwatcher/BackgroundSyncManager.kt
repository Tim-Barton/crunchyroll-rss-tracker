package com.example.crunchyrollwatcher

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class BackgroundSyncManager(private val context: Context) {

    companion object {
        const val RSS_CHECK_WORK_TAG = "rss_check_work_tag"
        const val ONE_TIME_CHECK_TAG = "one_time_rss_check"
        private const val DEFAULT_CHECK_INTERVAL_HOURS = 24L
    }

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedules daily RSS feed checking
     */
    fun scheduleDailyRssCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val rssCheckRequest = PeriodicWorkRequestBuilder<RssCheckWorker>(
            DEFAULT_CHECK_INTERVAL_HOURS,
            TimeUnit.HOURS,
            15, // Flex interval in minutes
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(RSS_CHECK_WORK_TAG)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            RssCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            rssCheckRequest
        )
    }

    /**
     * Schedules an immediate one-time RSS check
     */
    fun scheduleImmediateRssCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateCheckRequest = OneTimeWorkRequestBuilder<RssCheckWorker>()
            .setConstraints(constraints)
            .addTag(ONE_TIME_CHECK_TAG)
            .build()

        workManager.enqueue(immediateCheckRequest)
    }

    /**
     * Cancels all scheduled RSS checks
     */
    fun cancelRssChecks() {
        workManager.cancelUniqueWork(RssCheckWorker.WORK_NAME)
        workManager.cancelAllWorkByTag(RSS_CHECK_WORK_TAG)
        workManager.cancelAllWorkByTag(ONE_TIME_CHECK_TAG)
    }

    /**
     * Updates the RSS check schedule (cancels existing and creates new)
     */
    fun updateRssCheckSchedule(intervalHours: Long = DEFAULT_CHECK_INTERVAL_HOURS) {
        cancelRssChecks()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val rssCheckRequest = PeriodicWorkRequestBuilder<RssCheckWorker>(
            intervalHours,
            TimeUnit.HOURS,
            15,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(RSS_CHECK_WORK_TAG)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            RssCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            rssCheckRequest
        )
    }

    /**
     * Checks if RSS checking is currently scheduled
     */
    fun isRssCheckScheduled(): Boolean {
        return try {
            val workInfos = workManager.getWorkInfosForUniqueWork(RssCheckWorker.WORK_NAME).get()
            workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the current status of RSS check work
     */
    fun getRssCheckStatus(): WorkInfo.State? {
        return try {
            val workInfos = workManager.getWorkInfosForUniqueWork(RssCheckWorker.WORK_NAME).get()
            workInfos.firstOrNull()?.state
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Enable/disable background RSS checking based on user preference
     */
    fun setBackgroundCheckEnabled(enabled: Boolean) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("background_check_enabled", enabled).apply()

        if (enabled) {
            scheduleDailyRssCheck()
        } else {
            cancelRssChecks()
        }
    }

    /**
     * Check if background checking is enabled
     */
    fun isBackgroundCheckEnabled(): Boolean {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("background_check_enabled", true) // Default to enabled
    }

    /**
     * Get last successful check timestamp
     */
    fun getLastCheckTimestamp(): Long {
        val prefs = context.getSharedPreferences("rss_check_prefs", Context.MODE_PRIVATE)
        return prefs.getLong(RssCheckWorker.LAST_CHECK_PREF_KEY, 0L)
    }

    /**
     * Force sync - cancels existing work and starts immediate check
     */
    fun forceSyncNow() {
        // Cancel any pending work
        workManager.cancelAllWorkByTag(ONE_TIME_CHECK_TAG)

        // Schedule immediate check
        scheduleImmediateRssCheck()
    }
}
