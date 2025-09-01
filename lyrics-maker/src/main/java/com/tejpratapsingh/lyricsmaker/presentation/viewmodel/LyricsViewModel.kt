package com.tejpratapsingh.lyricsmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tejpratapsingh.lyricsmaker.data.api.client.LrcLibClient
import com.tejpratapsingh.lyricsmaker.data.api.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.data.api.model.SearchQuery

class LyricsViewModel : ViewModel() {

    private val _lyricsList = MutableLiveData<List<LyricsResponse>>()
    val lyricsList: LiveData<List<LyricsResponse>> = _lyricsList

    private val client = LrcLibClient()

    suspend fun fetchLyrics(query: String) {
        // This should ideally be done in a coroutine scope
        val results = client.searchLyrics(SearchQuery(query))
        _lyricsList.postValue(results)
    }
}