package com.tejpratapsingh.lyricsmaker.data.api.model

import kotlinx.serialization.Serializable

@Serializable
data class LyricsResponse(
    val id: Int,
    val trackName: String,
    val artistName: String,
    val albumName: String? = null,
    val duration: Float? = null,
    val instrumental: Boolean = false,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null
)
