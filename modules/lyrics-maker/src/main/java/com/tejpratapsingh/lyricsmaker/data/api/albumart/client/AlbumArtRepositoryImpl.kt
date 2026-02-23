package com.tejpratapsingh.lyricsmaker.data.api.albumart.client

import android.graphics.Bitmap

class AlbumArtRepositoryImpl(
    private val remote: AlbumArtRemoteDataSource,
) : AlbumArtRepository {
    override suspend fun getAlbumArtUrl(
        trackName: String,
        artistName: String,
        size: AlbumArtRepository.CoverSize,
    ): String? = remote.fetchAlbumArtUrl(trackName, artistName, size)

    override suspend fun getAlbumArtBitmap(url: String): Bitmap? = remote.fetchBitmap(url)
}
