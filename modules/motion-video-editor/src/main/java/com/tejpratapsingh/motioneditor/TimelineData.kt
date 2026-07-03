package com.tejpratapsingh.motioneditor

data class TimelineTrack(
    val id: String,
    val items: List<TimelineItem>,
)

data class TimelineItem(
    val id: String,
    val type: String,
    val startFrame: Int,
    val endFrame: Int,
    val label: String,
)
