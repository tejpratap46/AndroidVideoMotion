package com.tejpratapsingh.animator.notification

import NotificationChannelType
import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tejpratapsingh.animator.R

class NotificationFactory {
    companion object {
        // It's good practice to create channels once.
        // Consider moving this to Application.onCreate() or a dedicated initializer.
        // If kept here, ensure it's called appropriately (e.g., lazily or once per app session).
        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = NotificationManagerCompat.from(context)
                NotificationChannelType.entries.forEach { channelType ->
                    // Check if channel already exists to avoid re-creating unnecessarily
                    if (notificationManager.getNotificationChannel(channelType.channelId) == null) {
                        val channel = NotificationChannel(
                            channelType.channelId,
                            context.getString(channelType.channelNameResId), // Use string resource
                            channelType.importance
                        ).apply {
                            description =
                                context.getString(channelType.channelDescriptionResId) // Use string resource
                        }
                        notificationManager.createNotificationChannel(channel)
                    }
                }
            }
        }

        fun getRenderProgressNotification(
            context: Context
        ): NotificationCompat.Builder {
            val channelType = NotificationChannelType.RENDERING_PROGRESS

            // Ensure channel is created.
            // If you move channel creation to Application class, this explicit call might not be needed here.
            createNotificationChannels(context) // Or a more optimized way to ensure channels are created

            return NotificationCompat.Builder(context, channelType.channelId)
                .setSmallIcon(R.drawable.ic_notification_burst)
                .setContentTitle(context.getString(R.string.notification_render_progress_title)) // Use string resource
                .setContentText(context.getString(R.string.notification_render_progress_text_starting)) // Use string resource
                .setProgress(0, 0, true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
        }

        fun getRenderCompleteNotification(
            context: Context
        ): NotificationCompat.Builder {
            val channelType = NotificationChannelType.RENDERING_COMPLETED

            // Ensure channel is created
            createNotificationChannels(context) // Or a more optimized way to ensure channels are created

            return NotificationCompat.Builder(context, channelType.channelId)
                .setSmallIcon(R.drawable.ic_notification_burst)
                .setContentTitle(context.getString(R.string.notification_render_complete_title)) // Use string resource
                .setContentText(context.getString(R.string.notification_render_complete_text)) // Use string resource
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }
    }
}
