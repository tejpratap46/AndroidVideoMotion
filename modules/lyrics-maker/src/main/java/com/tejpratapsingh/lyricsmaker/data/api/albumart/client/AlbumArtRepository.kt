package com.tejpratapsingh.lyricsmaker.data.api.albumart.client

import android.graphics.Bitmap

interface AlbumArtRepository {
    enum class CoverSize(
        val suffix: String,
    ) {
        ORIGINAL(""),
        SMALL("-250"),
        MEDIUM("-500"),
        LARGE("-1200"),
    }

    suspend fun getAlbumArtUrl(
        trackName: String,
        artistName: String,
        size: CoverSize = CoverSize.SMALL,
    ): String?

    suspend fun getAlbumArtBitmap(url: String): Bitmap?
}
