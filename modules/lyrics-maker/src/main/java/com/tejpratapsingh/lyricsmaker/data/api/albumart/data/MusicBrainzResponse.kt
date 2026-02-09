package com.tejpratapsingh.lyricsmaker.data.api.albumart.data

import kotlinx.serialization.Serializable

@Serializable
data class MusicBrainzResponse(
    val recordings: List<Recording> = emptyList(),
)
