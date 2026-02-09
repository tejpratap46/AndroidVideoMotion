package com.tejpratapsingh.lyricsmaker.data.api.lrclib.client

import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.GetParams
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.SearchParams

/**
 * Implementation of LyricsRepository using LRCLIB API
 */
class LyricsRepositoryImpl(
    private val apiService: LrcLibApiService,
) : LyricsRepository {
    override suspend fun searchLyrics(params: SearchParams): Result<List<LyricsResponse>> = apiService.search(params)

    override suspend fun getLyrics(params: GetParams): Result<LyricsResponse> = apiService.get(params)
}
