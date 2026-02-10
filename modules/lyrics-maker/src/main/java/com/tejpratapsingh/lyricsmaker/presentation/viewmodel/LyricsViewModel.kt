package com.tejpratapsingh.lyricsmaker.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tejpratapsingh.lyricsmaker.data.api.client.LrcLibClient
import com.tejpratapsingh.lyricsmaker.data.api.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.data.api.model.SearchQuery
import com.tejpratapsingh.lyricsmaker.data.lrc.LrcHelper
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.utils.getSyncedLyricFrameStringList
import com.tejpratapsingh.motion.metadataextractor.data.SocialMeta
import com.tejpratapsingh.motion.ongoing.domain.CurrentProject
import com.tejpratapsingh.motion.ongoing.domain.ProjectManager
import com.tejpratapsingh.motionlib.core.MotionConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class LyricsViewModel(val projectManager: ProjectManager = ProjectManager) : ViewModel() {
    val socialMeta = MutableStateFlow<SocialMeta?>(null)
    val query = MutableStateFlow("")
    val isLoading = MutableStateFlow(false)
    private val _lyricsList = MutableStateFlow<List<LyricsResponse>>(emptyList())
    val lyricsList: Flow<List<LyricsResponse>> = _lyricsList
    private val _dashboardData = MutableStateFlow<List<CurrentProject>>(emptyList())
    val dashboardData: StateFlow<List<CurrentProject>> = _dashboardData
    private val client = LrcLibClient()

    init {
        viewModelScope.launch {
            socialMeta.collect { socialMeta ->
                Log.d("receive socialMeta", "$socialMeta")
                socialMeta?.let {
                    with(projectManager.songProject) {
                        title = it.title
                        description = it.description
                        image = it.image
                        siteName = it.siteName
                        twitterCard = it.twitterCard
                        url = it.url
                    }
                    Log.d("receive songProject", "${projectManager.songProject}")
                }
            }
        }
    }

    var selectedLyricResponse: LyricsResponse = LyricsResponse(
        id = 0,
        trackName = "",
        artistName = "",
    )
        set(value) {
            field = value
            with(projectManager.songProject) {
                trackName = field.trackName
                artistName = field.artistName
                albumName = field.albumName
                duration = field.duration
                instrumental = field.instrumental
                plainLyrics = field.plainLyrics
                syncedLyrics = field.syncedLyrics
            }
        }

    val selectedSongName: String
        get() = "${selectedLyricResponse.trackName} - ${selectedLyricResponse.artistName}"

    val lyrics: List<SyncedLyricFrame>
        get() =
            LrcHelper.getSyncedLyrics(
                lrcContent = selectedLyricResponse.getLyrics(),
                fps = MotionConfig.fps,
            ).also {
                ProjectManager.songProject.totalLyrics = getSyncedLyricFrameStringList(it)
            }

    var selectedLyrics: List<SyncedLyricFrame> = emptyList()
        get() {
            val firstFrame = field.first().frame
            return field
                .map {
                    SyncedLyricFrame(
                        frame = it.frame - firstFrame,
                        text = it.text,
                    )
                }.sortedBy { it.frame }
        }
        set(value) {
            field = value
            ProjectManager.songProject.apply {
                selectedLyrics = field.joinToString(separator = ",") {
                    Log.d("ooo","formatted line ${it.line()}")
                    it.line()
                }
            }
            Log.d("ooo","finalSelected ${ProjectManager.songProject.selectedLyrics}")
        }

    var currentSelectedProject: CurrentProject?=null

    suspend fun fetchLyrics() {
        isLoading.value = true
        val results =
            client.searchLyrics(SearchQuery(query.value)).filter {
                it.syncedLyrics != null
            }
        _lyricsList.emit(results)
        isLoading.value = false
    }
    fun loadDashboardData() {
        viewModelScope.launch {
            projectManager.getAllProjects()?.let {
                _dashboardData.value = it
            }.also {
                Log.d("savedData", "$it")
            }
        }
    }
}
