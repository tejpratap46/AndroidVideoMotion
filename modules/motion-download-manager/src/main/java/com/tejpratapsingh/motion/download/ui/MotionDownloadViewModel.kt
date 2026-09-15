package com.tejpratapsingh.motion.download.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tejpratapsingh.motion.download.MotionAssetManagerImpl
import com.tejpratapsingh.motionstore.tables.MotionProject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MotionDownloadViewModel(
    private val downloadManager: MotionAssetManagerImpl,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MotionDownloadUiState>(MotionDownloadUiState.Idle)
    val uiState: StateFlow<MotionDownloadUiState> = _uiState.asStateFlow()

    private var downloadJob: Job? = null

    fun startDownload(project: MotionProject) {
        downloadJob?.cancel()
        _uiState.value = MotionDownloadUiState.Idle
        downloadJob = viewModelScope.launch {
            downloadManager.downloadAssets(project).collect { progress ->
                if (progress.isComplete) {
                    if (progress.error != null) {
                        _uiState.value =
                            MotionDownloadUiState.Error(
                                message = progress.error,
                                assetProgressList = progress.assetProgressList,
                            )
                    } else {
                        _uiState.value =
                            MotionDownloadUiState.Success(
                                assetProgressList = progress.assetProgressList,
                            )
                    }
                } else {
                    _uiState.value =
                        MotionDownloadUiState.Downloading(
                            totalFiles = progress.totalFiles,
                            downloadedFiles = progress.downloadedFiles,
                            progress = progress.currentProgress,
                            assetProgressList = progress.assetProgressList,
                        )
                }
            }
        }
    }

    fun retryAsset(id: Int) {
        downloadManager.retryAsset(id)
    }

    fun hasPendingDownloads(project: MotionProject): Boolean = downloadManager.hasPendingDownloads(project)

    fun reset() {
        downloadJob?.cancel()
        downloadJob = null
        _uiState.value = MotionDownloadUiState.Idle
    }
}
