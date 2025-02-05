package com.github.ParagonVirtuoso.memorias.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.ParagonVirtuoso.memorias.MainActivity
import com.github.ParagonVirtuoso.memorias.R
import com.github.ParagonVirtuoso.memorias.domain.repository.MemoryRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class MemoryNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val memoryRepository: MemoryRepository,
    private val fcm: FirebaseMessaging
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val videoId = inputData.getString(KEY_VIDEO_ID)
        val videoTitle = inputData.getString(KEY_VIDEO_TITLE)
        val videoThumbnail = inputData.getString(KEY_VIDEO_THUMBNAIL)
        val notificationTime = inputData.getLong(KEY_NOTIFICATION_TIME, 0)
        val currentTime = System.currentTimeMillis()

        if (videoId == null || videoTitle == null || videoThumbnail == null) {
            return Result.retry()
        }

        val timeDifference = currentTime - notificationTime
        if (timeDifference > TimeUnit.MINUTES.toMillis(5)) {
            return Result.failure()
        }

        return try {
            createNotificationChannel()
            showNotification(videoId, videoTitle, videoThumbnail)

            try {
                memoryRepository.markAsNotified(videoId)
            } catch (e: Exception) {
                // Não falhar o worker se não conseguir marcar como notificada
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.memory_notification_channel_name)
            val description = context.getString(R.string.memory_notification_channel_description)

            val channel = NotificationChannel(
                CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                this.description = description
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(videoId: String, videoTitle: String, videoThumbnail: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(KEY_VIDEO_ID, videoId)
            putExtra(KEY_VIDEO_TITLE, videoTitle)
            putExtra(KEY_VIDEO_THUMBNAIL, videoThumbnail)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            videoId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.memory_notification_title))
            .setContentText(context.getString(R.string.memory_notification_text, videoTitle))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setFullScreenIntent(pendingIntent, true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = videoId.hashCode()
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "memory_notifications"
        const val KEY_VIDEO_ID = "video_id"
        const val KEY_VIDEO_TITLE = "video_title"
        const val KEY_VIDEO_THUMBNAIL = "video_thumbnail"
        const val KEY_NOTIFICATION_TIME = "notification_time"
    }
}