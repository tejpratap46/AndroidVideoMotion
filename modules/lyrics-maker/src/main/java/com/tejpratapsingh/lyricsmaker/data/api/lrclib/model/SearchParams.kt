package com.tejpratapsingh.lyricsmaker.data.api.lrclib.model

data class SearchParams(
    val trackName: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val q: String? = null, // General search query
)
