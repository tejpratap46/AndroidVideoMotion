package com.tejpratapsingh.lyricsmaker.presentation.compose.projects

import com.tejpratapsingh.motionstore.tables.MotionProject

internal fun MotionProject.updatedLabel(): String {
    val diff = System.currentTimeMillis() - updated
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> "${diff / 86_400_000}d ago"
    }
}
