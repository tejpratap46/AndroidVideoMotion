package com.tejpratapsingh.lyricsmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.client.LrcLibApiService
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.client.LrcLibApiServiceImpl
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.client.LyricsRepository
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.client.LyricsRepositoryImpl
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.SearchParams
import com.tejpratapsingh.lyricsmaker.data.lrc.LrcHelper
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.di.OkHttpProvider
import com.tejpratapsingh.motion.metadataextractor.data.SocialMeta
import com.tejpratapsingh.motionlib.core.extensions.md5
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionstore.tables.MotionProject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * UI state for lyrics screen
 */
sealed class LyricsUiState {
    object Initial : LyricsUiState()

    object Loading : LyricsUiState()

    data class Success(
        val lyrics: List<LyricsResponse>,
    ) : LyricsUiState()

    data class Error(
        val message: String,
    ) : LyricsUiState()
}

/**
 * ViewModel for lyrics operations
 */
class LyricsViewModel : ViewModel() {
    private val okHttpClient = OkHttpProvider.httpClient
    private val gson = Gson()
    private val apiService: LrcLibApiService = LrcLibApiServiceImpl(okHttpClient, gson)
    private val repository: LyricsRepository = LyricsRepositoryImpl(apiService)

    private val _uiState = MutableStateFlow<LyricsUiState>(LyricsUiState.Initial)
    val uiState: StateFlow<LyricsUiState> = _uiState.asStateFlow()
    val query: MutableStateFlow<String> = MutableStateFlow("")
    val socialMeta: MutableStateFlow<SocialMeta> = MutableStateFlow(SocialMeta())
    val selectedLyric: MutableStateFlow<LyricsResponse?> = MutableStateFlow(null)

    /**
     * Search for lyrics
     */
    fun searchLyrics(
        trackName: String? = null,
        artistName: String? = null,
        albumName: String? = null,
        query: String? = null,
    ) {
        Timber.d("searchLyrics: track=$trackName, artist=$artistName, q=$query")
        viewModelScope.launch {
            _uiState.value = LyricsUiState.Loading

            val params =
                SearchParams(
                    trackName = trackName,
                    artistName = artistName,
                    albumName = albumName,
                    q = query,
                )

            repository
                .searchLyrics(params)
                .onSuccess { lyrics ->
                    Timber.d("searchLyrics success: found ${lyrics.size} results")
                    _uiState.value = LyricsUiState.Success(lyrics)
                }.onFailure { error ->
                    Timber.e(error, "searchLyrics failure")
                    _uiState.value =
                        LyricsUiState.Error(
                            error.message ?: "Unknown error occurred",
                        )
                }
        }
    }

    val selectedSongName: String
        get() = "${selectedLyric.value?.trackName} - ${selectedLyric.value?.artistName}"

    val lyrics: List<SyncedLyricFrame>
        get() {
            return if (selectedLyric.value == null) {
                emptyList()
            } else {
                LrcHelper.getSyncedLyrics(
                    lrcContent = selectedLyric.value?.getLyrics() ?: "",
                    fps = provideCurrentConfig().fps,
                )
            }
        }

    var selectedStartTimeInSeconds: Float = 0f

    var selectedLyrics: List<SyncedLyricFrame> = emptyList()
        set(value) {
            field = value
            selectedStartTimeInSeconds =
                if (value.isNotEmpty()) {
                    value.first().frame.toFloat() / provideCurrentConfig().fps
                } else {
                    0f
                }
        }
        get() {
            if (field.isEmpty()) return emptyList()
            val firstFrame = field.first().frame
            return field
                .map {
                    SyncedLyricFrame(
                        frame = it.frame - firstFrame,
                        text = it.text,
                    )
                }.sortedBy { it.frame }
        }

    fun createMotionProject(): MotionProject {
        val projectId = selectedSongName.md5()
        val image = socialMeta.value.image

        return MotionProject(
            id = projectId,
            name = selectedSongName,
            path = "/$projectId",
            metadata =
                JsonObject().apply {
                    addProperty("image", image)
                    addProperty("startTime", selectedStartTimeInSeconds)
                    add(
                        "lyrics",
                        JsonArray().apply {
                            selectedLyrics.forEach { frame ->
                                add(
                                    JsonObject().apply {
                                        addProperty("frame", frame.frame)
                                        addProperty("text", frame.text)
                                    },
                                )
                            }
                        },
                    )
                },
        )
    }
}
