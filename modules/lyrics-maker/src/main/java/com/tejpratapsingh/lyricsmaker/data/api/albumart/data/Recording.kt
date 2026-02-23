package com.tejpratapsingh.lyricsmaker.data.api.albumart.data

import kotlinx.serialization.Serializable

@Serializable
data class Recording(
    val releases: List<Release> = emptyList(),
)
