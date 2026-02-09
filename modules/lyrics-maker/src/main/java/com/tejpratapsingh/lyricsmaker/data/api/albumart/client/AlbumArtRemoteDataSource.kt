package com.tejpratapsingh.lyricsmaker.data.api.albumart.client

import android.graphics.Bitmap

interface AlbumArtRemoteDataSource {
    suspend fun fetchAlbumArtUrl(
        trackName: String,
        artistName: String,
        size: AlbumArtRepository.CoverSize,
    ): String?

    suspend fun fetchBitmap(url: String): Bitmap?
}
