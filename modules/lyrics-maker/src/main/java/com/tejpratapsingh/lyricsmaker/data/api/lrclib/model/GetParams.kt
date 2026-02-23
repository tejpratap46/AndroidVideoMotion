package com.tejpratapsingh.lyricsmaker.data.api.lrclib.model

data class GetParams(
    val trackName: String,
    val artistName: String,
    val albumName: String? = null,
    val duration: Int? = null,
)
