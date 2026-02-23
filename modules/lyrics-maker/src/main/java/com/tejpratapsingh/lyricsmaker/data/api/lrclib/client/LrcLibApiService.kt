package com.tejpratapsingh.lyricsmaker.data.api.lrclib.client

import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.GetParams
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.SearchParams

/**
 * API service interface for LRCLIB
 */
interface LrcLibApiService {
    /**
     * Search for lyrics
     * @param params Search parameters
     * @return List of matching lyrics
     */
    suspend fun search(params: SearchParams): Result<List<LyricsResponse>>

    /**
     * Get specific lyrics by track metadata
     * @param params Get parameters with track information
     * @return Single lyrics result
     */
    suspend fun get(params: GetParams): Result<LyricsResponse>
}
