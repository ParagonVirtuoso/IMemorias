package com.github.ParagonVirtuoso.memorias.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.github.ParagonVirtuoso.memorias.MainActivity
import com.github.ParagonVirtuoso.memorias.R
import com.github.ParagonVirtuoso.memorias.worker.MemoryNotificationWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MemoryNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val videoId = intent.getStringExtra(MemoryNotificationWorker.KEY_VIDEO_ID)
        val videoTitle = intent.getStringExtra(MemoryNotificationWorker.KEY_VIDEO_TITLE)
        val videoThumbnail = intent.getStringExtra(MemoryNotificationWorker.KEY_VIDEO_THUMBNAIL)
        val notificationTime = intent.getLongExtra(MemoryNotificationWorker.KEY_NOTIFICATION_TIME, 0)

        if (videoId == null || videoTitle == null || videoThumbnail == null) {
            return
        }

        createNotificationChannel(context)
        showNotification(context, videoId, videoTitle, videoThumbnail)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.memory_notification_channel_name)
            val description = context.getString(R.string.memory_notification_channel_description)

            val channel = NotificationChannel(
                MemoryNotificationWorker.CHANNEL_ID,
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

    private fun showNotification(context: Context, videoId: String, videoTitle: String, videoThumbnail: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(MemoryNotificationWorker.KEY_VIDEO_ID, videoId)
            putExtra(MemoryNotificationWorker.KEY_VIDEO_TITLE, videoTitle)
            putExtra(MemoryNotificationWorker.KEY_VIDEO_THUMBNAIL, videoThumbnail)
            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            videoId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, MemoryNotificationWorker.CHANNEL_ID)
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
} 