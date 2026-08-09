package com.tejpratapsingh.motion.download.ui

import com.tejpratapsingh.motion.download.model.AssetDownloadProgress

sealed class MotionDownloadUiState {
    object Idle : MotionDownloadUiState()
    
    data class Downloading(
        val totalFiles: Int,
        val downloadedFiles: Int,
        val progress: Int, // 0-100 overall
        val assetProgressList: List<AssetDownloadProgress>
    ) : MotionDownloadUiState()
    
    data class Success(
        val assetProgressList: List<AssetDownloadProgress>
    ) : MotionDownloadUiState()
    
    data class Error(
        val message: String,
        val assetProgressList: List<AssetDownloadProgress>
    ) : MotionDownloadUiState()
}
