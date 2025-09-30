package com.example.crunchyrollwatcher

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "anime_episodes_channel"
        const val CHANNEL_NAME = "New Anime Episodes"
        const val CHANNEL_DESCRIPTION = "Notifications for new episodes of tracked anime series"
        const val NOTIFICATION_ID_BASE = 1000
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNewEpisodeNotification(episode: CrunchyrollEpisode, savedTitle: SavedTitle) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("search_query", episode.seriesTitle)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationTitle = "New Episode Available!"
        val notificationText = "${episode.seriesTitle} - ${episode.title}"
        val bigText = buildString {
            if (!episode.episodeNumber.isNullOrEmpty()) {
                append("Episode ${episode.episodeNumber}\n")
            }
            append(episode.description.take(200))
            if (episode.description.length > 200) {
                append("...")
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
                    .setBigContentTitle(notificationTitle)
                    .setSummaryText(episode.seriesTitle)
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = NOTIFICATION_ID_BASE + savedTitle.id.hashCode()

            // Load image if available and update notification
            if (!episode.imageUrl.isNullOrEmpty()) {
                loadImageAndUpdateNotification(
                    episode.imageUrl,
                    notification,
                    notificationId,
                    notificationManager
                )
            } else {
                notificationManager.notify(notificationId, notification)
            }
        } catch (e: SecurityException) {
            // Notification permission not granted
            println("NotificationHelper: Permission not granted for notifications")
        }
    }

    fun showMultipleEpisodesNotification(newEpisodes: List<Pair<CrunchyrollEpisode, SavedTitle>>) {
        if (newEpisodes.isEmpty()) return

        val intent = Intent(context, SavedTitlesActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val episodeCount = newEpisodes.size
        val seriesCount = newEpisodes.map { it.second.title }.distinct().size

        val notificationTitle = if (seriesCount == 1) {
            "New episodes for ${newEpisodes.first().second.title}"
        } else {
            "$episodeCount new episodes available"
        }

        val notificationText = if (seriesCount == 1) {
            "$episodeCount new episodes"
        } else {
            "From $seriesCount tracked series"
        }

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(notificationTitle)
            .setSummaryText("$episodeCount new episodes")

        newEpisodes.take(7).forEach { (episode, savedTitle) ->
            val line = buildString {
                append(savedTitle.title)
                if (!episode.episodeNumber.isNullOrEmpty()) {
                    append(" - Episode ${episode.episodeNumber}")
                } else {
                    append(" - ${episode.title.take(30)}")
                    if (episode.title.length > 30) append("...")
                }
            }
            inboxStyle.addLine(line)
        }

        if (newEpisodes.size > 7) {
            inboxStyle.addLine("And ${newEpisodes.size - 7} more...")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(inboxStyle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setNumber(episodeCount)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID_BASE, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
            println("NotificationHelper: Permission not granted for notifications")
        }
    }

    private fun loadImageAndUpdateNotification(
        imageUrl: String,
        notification: android.app.Notification,
        notificationId: Int,
        notificationManager: NotificationManagerCompat
    ) {
        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val updatedNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(notification.extras.getString(NotificationCompat.EXTRA_TITLE))
                        .setContentText(notification.extras.getString(NotificationCompat.EXTRA_TEXT))
                        .setStyle(
                            NotificationCompat.BigTextStyle()
                                .bigText(notification.extras.getString(NotificationCompat.EXTRA_BIG_TEXT))
                        )
                        .setLargeIcon(resource)
                        .setContentIntent(notification.contentIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                        .build()

                    try {
                        notificationManager.notify(notificationId, updatedNotification)
                    } catch (e: SecurityException) {
                        // Fallback to original notification
                        notificationManager.notify(notificationId, notification)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Show original notification without image
                    try {
                        notificationManager.notify(notificationId, notification)
                    } catch (e: SecurityException) {
                        // Permission not granted
                    }
                }
            })
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true
        }
    }

    fun cancelAllNotifications() {
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancelAll()
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun cancelNotification(notificationId: Int) {
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(notificationId)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
