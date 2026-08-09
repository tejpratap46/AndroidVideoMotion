package com.tejpratapsingh.motion.download.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tejpratapsingh.motion.download.MotionDownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MotionDownloadViewModel(
    private val downloadManager: MotionDownloadManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<MotionDownloadUiState>(MotionDownloadUiState.Idle)
    val uiState: StateFlow<MotionDownloadUiState> = _uiState.asStateFlow()

    fun startDownload(sduiJson: String) {
        viewModelScope.launch {
            downloadManager.downloadAssets(sduiJson).collect { progress ->
                if (progress.isComplete) {
                    if (progress.error != null) {
                        _uiState.value = MotionDownloadUiState.Error(
                            message = progress.error,
                            assetProgressList = progress.assetProgressList
                        )
                    } else {
                        _uiState.value = MotionDownloadUiState.Success(
                            assetProgressList = progress.assetProgressList
                        )
                    }
                } else {
                    _uiState.value = MotionDownloadUiState.Downloading(
                        totalFiles = progress.totalFiles,
                        downloadedFiles = progress.downloadedFiles,
                        progress = progress.currentProgress,
                        assetProgressList = progress.assetProgressList
                    )
                }
            }
        }
    }

    fun retryAsset(id: Int) {
        downloadManager.retryAsset(id)
    }

    fun hasPendingDownloads(sduiJson: String): Boolean {
        return downloadManager.hasPendingDownloads(sduiJson)
    }

    fun reset() {
        _uiState.value = MotionDownloadUiState.Idle
    }
}
