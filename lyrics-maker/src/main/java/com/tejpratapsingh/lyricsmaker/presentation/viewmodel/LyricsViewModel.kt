package com.tejpratapsingh.lyricsmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.tejpratapsingh.lyricsmaker.data.api.client.LrcLibClient
import com.tejpratapsingh.lyricsmaker.data.api.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.data.api.model.SearchQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

open class LyricsViewModel : ViewModel() {

    private val _lyricsList = MutableStateFlow<List<LyricsResponse>>(emptyList())
    val lyricsList: Flow<List<LyricsResponse>> = _lyricsList

    private val client = LrcLibClient()

    suspend fun fetchLyrics(query: String) {
        // This should ideally be done in a coroutine scope
        val results = client.searchLyrics(SearchQuery(query)).filter {
            it.syncedLyrics != null
        }
        _lyricsList.emit(results)
    }
}