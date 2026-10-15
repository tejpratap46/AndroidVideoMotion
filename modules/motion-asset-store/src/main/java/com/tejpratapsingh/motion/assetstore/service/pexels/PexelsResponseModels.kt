package com.tejpratapsingh.motion.assetstore.service.pexels

import com.google.gson.annotations.SerializedName

data class PexelsPhotoResponse(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("per_page") val perPage: Int = 30,
    @SerializedName("total_results") val totalResults: Int = 0,
    @SerializedName("next_page") val nextPage: String? = null,
    @SerializedName("photos") val photos: List<PexelsPhotoDto> = emptyList(),
)

data class PexelsPhotoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("width") val width: Int = 0,
    @SerializedName("height") val height: Int = 0,
    @SerializedName("url") val url: String = "",
    @SerializedName("photographer") val photographer: String? = null,
    @SerializedName("photographer_url") val photographerUrl: String? = null,
    @SerializedName("alt") val alt: String? = null,
    @SerializedName("src") val src: PexelsPhotoSourceDto? = null,
)

data class PexelsPhotoSourceDto(
    @SerializedName("original") val original: String = "",
    @SerializedName("large2x") val large2x: String? = null,
    @SerializedName("large") val large: String? = null,
    @SerializedName("medium") val medium: String? = null,
    @SerializedName("small") val small: String? = null,
    @SerializedName("portrait") val portrait: String? = null,
    @SerializedName("landscape") val landscape: String? = null,
    @SerializedName("tiny") val tiny: String? = null,
)

data class PexelsVideoResponse(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("per_page") val perPage: Int = 30,
    @SerializedName("total_results") val totalResults: Int = 0,
    @SerializedName("next_page") val nextPage: String? = null,
    @SerializedName("videos") val videos: List<PexelsVideoDto> = emptyList(),
)

data class PexelsVideoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("width") val width: Int = 0,
    @SerializedName("height") val height: Int = 0,
    @SerializedName("url") val url: String = "",
    @SerializedName("image") val image: String = "",
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("user") val user: PexelsUserDto? = null,
    @SerializedName("video_files") val videoFiles: List<PexelsVideoFileDto> = emptyList(),
)

data class PexelsUserDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String? = null,
    @SerializedName("url") val url: String? = null,
)

data class PexelsVideoFileDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("quality") val quality: String? = null,
    @SerializedName("file_type") val fileType: String? = null,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null,
    @SerializedName("link") val link: String = "",
)
