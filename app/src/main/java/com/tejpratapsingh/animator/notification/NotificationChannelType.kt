package com.tejpratapsingh.animator.notification

import androidx.core.app.NotificationManagerCompat

enum class NotificationChannelType(
    val channelId: String,
    val channelName: String,
    val channelDescription: String,
    val importance: Int
) {
    RENDERING_PROGRESS(
        "rendering_progress",
        "Rendering In Progress",
        "Progress of rendered videos",
        NotificationManagerCompat.IMPORTANCE_LOW
    ),

    RENDERING_COMPLETED(
        "rendering_completed",
        "Rendering Completed",
        "Rendering Completed",
        NotificationManagerCompat.IMPORTANCE_HIGH
    )
}