package com.tejpratapsingh.animator.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tejpratapsingh.animator.R

class NotificationFactory {
    companion object {
        fun getRenderProgressNotification(
            context: Context
        ): NotificationCompat.Builder {
            val channel: NotificationChannelType = NotificationChannelType.RENDERING_PROGRESS

            createNotificationChannel(context = context, channel = channel)

            return NotificationCompat.Builder(context, channel.channelId)
                .setSmallIcon(R.drawable.ic_notification_burst)
                .setContentTitle("Rendering In Progress")
                .setContentText("Starting Render")
                .setProgress(0, 0, true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
        }

        fun getRenderCompleteNotification(
            context: Context
        ): NotificationCompat.Builder {
            val channel: NotificationChannelType = NotificationChannelType.RENDERING_COMPLETED

            createNotificationChannel(context = context, channel = channel)

            return NotificationCompat.Builder(context, channel.channelId)
                .setSmallIcon(R.drawable.ic_notification_burst)
                .setContentTitle("Rendering Finished")
                .setContentText("Video Rendering finished")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

        private fun createNotificationChannel(context: Context, channel: NotificationChannelType) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    channel.channelId,
                    channel.channelName,
                    channel.importance
                ).apply {
                    description = channel.channelDescription
                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }
}