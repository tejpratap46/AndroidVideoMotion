package com.tejpratapsingh.motioneditor

import com.google.gson.JsonObject

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
    val original: Any? = null,
    val sdui: JsonObject? = null,
)
