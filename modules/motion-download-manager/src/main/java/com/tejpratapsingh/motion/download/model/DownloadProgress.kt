package com.tejpratapsingh.motion.download.model

data class AssetDownloadProgress(
    val id: Int,
    val url: String,
    val fileName: String,
    val progress: Int,
    val status: String, // e.g., "QUEUED", "PROGRESS", "SUCCESS", "FAILED"
    val error: String? = null,
)

data class DownloadProgress(
    val totalFiles: Int,
    val downloadedFiles: Int,
    val currentProgress: Int, // 0-100 overall progress
    val assetProgressList: List<AssetDownloadProgress> = emptyList(),
    val isComplete: Boolean = false,
    val error: String? = null,
)
