package com.tejpratapsingh.lyricsmaker.data.api.lrclib.client

import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.GetParams
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.SearchParams

/**
 * Repository interface for lyrics data
 */
interface LyricsRepository {
    /**
     * Search for lyrics by various criteria
     */
    suspend fun searchLyrics(params: SearchParams): Result<List<LyricsResponse>>

    /**
     * Get specific lyrics by track metadata
     */
    suspend fun getLyrics(params: GetParams): Result<LyricsResponse>
}
