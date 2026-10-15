package com.tejpratapsingh.motion.assetstore.model

sealed interface AssetItem {
    val id: String
    val title: String
    val previewUrl: String
    val downloadUrl: String
    val authorName: String?
    val authorUrl: String?
    val sourceName: String

    data class Image(
        override val id: String,
        override val title: String,
        override val previewUrl: String,
        override val downloadUrl: String,
        override val authorName: String? = null,
        override val authorUrl: String? = null,
        override val sourceName: String = "Pexels",
        val width: Int = 0,
        val height: Int = 0,
    ) : AssetItem

    data class Video(
        override val id: String,
        override val title: String,
        override val previewUrl: String,
        override val downloadUrl: String,
        override val authorName: String? = null,
        override val authorUrl: String? = null,
        override val sourceName: String = "Pexels",
        val width: Int = 0,
        val height: Int = 0,
        val durationSeconds: Int = 0,
        val videoFiles: List<VideoQualityFile> = emptyList(),
    ) : AssetItem
}

data class VideoQualityFile(
    val quality: String,
    val link: String,
    val width: Int? = null,
    val height: Int? = null,
    val fileType: String? = null,
)
