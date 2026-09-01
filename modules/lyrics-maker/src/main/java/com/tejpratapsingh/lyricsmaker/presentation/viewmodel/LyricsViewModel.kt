package com.tejpratapsingh.lyricsmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.client.LyricsRepository
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.SearchParams
import com.tejpratapsingh.lyricsmaker.data.lrc.LrcHelper
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motion.metadataextractor.data.SocialMeta
import com.tejpratapsingh.motionlib.core.MotionConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
class LyricsViewModel(
    private val repository: LyricsRepository,
) : ViewModel() {
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
                    _uiState.value = LyricsUiState.Success(lyrics)
                }.onFailure { error ->
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
                    fps = MotionConfig().fps,
                )
            }
        }

    var selectedStartTimeInSeconds: Float = 0f

    var selectedLyrics: List<SyncedLyricFrame> = emptyList()
        set(value) {
            field = value
            selectedStartTimeInSeconds =
                if (value.isNotEmpty()) {
                    value.first().frame.toFloat() / MotionConfig().fps
                } else {
                    0f
                }
        }
        get() {
            if (field.isEmpty()) return emptyList()
            val sortedField = field.sortedBy { it.frame }
            val firstFrame = sortedField.first().frame
            return sortedField
                .map {
                    SyncedLyricFrame(
                        frame = it.frame - firstFrame,
                        text = it.text,
                    )
                }
        }
}
