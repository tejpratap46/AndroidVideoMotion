package com.tejpratapsingh.lyricsmaker.data.api.model

import kotlinx.serialization.Serializable

@Serializable
data class LyricsQuery(
    val id: Int? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val duration: Int? = null
)