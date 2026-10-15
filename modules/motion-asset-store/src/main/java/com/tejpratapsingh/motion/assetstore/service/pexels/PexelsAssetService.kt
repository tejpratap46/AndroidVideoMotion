package com.tejpratapsingh.motion.assetstore.service.pexels

import com.google.gson.Gson
import com.tejpratapsingh.motion.assetstore.model.AssetFileType
import com.tejpratapsingh.motion.assetstore.model.AssetItem
import com.tejpratapsingh.motion.assetstore.model.AssetSearchResult
import com.tejpratapsingh.motion.assetstore.model.VideoQualityFile
import com.tejpratapsingh.motion.assetstore.service.ExternalAssetService
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import timber.log.Timber

class PexelsAssetService(
    private val client: HttpClient,
    private val gson: Gson,
    private val config: PexelsConfig = PexelsConfig(),
) : ExternalAssetService {
    override val serviceId: String = "pexels"
    override val serviceName: String = "Pexels"
    override val supportedTypes: Set<AssetFileType> = setOf(AssetFileType.IMAGE, AssetFileType.VIDEO)

    private val baseUrl = "https://api.pexels.com"

    override suspend fun searchImages(
        query: String,
        page: Int,
        perPage: Int,
    ): Result<AssetSearchResult<AssetItem.Image>> =
        runCatching {
            val responseText =
                client.get("$baseUrl/v1/search") {
                    header("Authorization", config.apiKey)
                    parameter("query", query)
                    parameter("page", page)
                    parameter("per_page", perPage)
                }.bodyAsText()

            val response = gson.fromJson(responseText, PexelsPhotoResponse::class.java)
            mapPhotoResponse(response)
        }.onFailure {
            Timber.e(it, "Failed to search images on Pexels for query: $query")
        }

    override suspend fun searchVideos(
        query: String,
        page: Int,
        perPage: Int,
    ): Result<AssetSearchResult<AssetItem.Video>> =
        runCatching {
            val responseText =
                client.get("$baseUrl/videos/search") {
                    header("Authorization", config.apiKey)
                    parameter("query", query)
                    parameter("page", page)
                    parameter("per_page", perPage)
                }.bodyAsText()

            val response = gson.fromJson(responseText, PexelsVideoResponse::class.java)
            mapVideoResponse(response)
        }.onFailure {
            Timber.e(it, "Failed to search videos on Pexels for query: $query")
        }

    override suspend fun getCuratedImages(
        page: Int,
        perPage: Int,
    ): Result<AssetSearchResult<AssetItem.Image>> =
        runCatching {
            val responseText =
                client.get("$baseUrl/v1/curated") {
                    header("Authorization", config.apiKey)
                    parameter("page", page)
                    parameter("per_page", perPage)
                }.bodyAsText()

            val response = gson.fromJson(responseText, PexelsPhotoResponse::class.java)
            mapPhotoResponse(response)
        }.onFailure {
            Timber.e(it, "Failed to fetch curated images on Pexels")
        }

    override suspend fun getPopularVideos(
        page: Int,
        perPage: Int,
    ): Result<AssetSearchResult<AssetItem.Video>> =
        runCatching {
            val responseText =
                client.get("$baseUrl/videos/popular") {
                    header("Authorization", config.apiKey)
                    parameter("page", page)
                    parameter("per_page", perPage)
                }.bodyAsText()

            val response = gson.fromJson(responseText, PexelsVideoResponse::class.java)
            mapVideoResponse(response)
        }.onFailure {
            Timber.e(it, "Failed to fetch popular videos on Pexels")
        }

    private fun mapPhotoResponse(response: PexelsPhotoResponse): AssetSearchResult<AssetItem.Image> {
        val items =
            response.photos.map { dto ->
                AssetItem.Image(
                    id = dto.id.toString(),
                    title = dto.alt?.takeIf { it.isNotBlank() } ?: "Pexels Photo ${dto.id}",
                    previewUrl = dto.src?.medium ?: dto.src?.small ?: dto.src?.original ?: "",
                    downloadUrl = dto.src?.original ?: dto.src?.large2x ?: dto.src?.medium ?: "",
                    authorName = dto.photographer,
                    authorUrl = dto.photographerUrl,
                    sourceName = serviceName,
                    width = dto.width,
                    height = dto.height,
                )
            }

        return AssetSearchResult(
            items = items,
            page = response.page,
            perPage = response.perPage,
            totalResults = response.totalResults,
            hasNextPage = response.nextPage != null,
        )
    }

    private fun mapVideoResponse(response: PexelsVideoResponse): AssetSearchResult<AssetItem.Video> {
        val items =
            response.videos.map { dto ->
                val bestFile =
                    dto.videoFiles.firstOrNull { it.quality == "hd" }
                        ?: dto.videoFiles.firstOrNull { it.quality == "sd" }
                        ?: dto.videoFiles.firstOrNull()

                val videoFiles =
                    dto.videoFiles.map { vf ->
                        VideoQualityFile(
                            quality = vf.quality ?: "sd",
                            link = vf.link,
                            width = vf.width,
                            height = vf.height,
                            fileType = vf.fileType,
                        )
                    }

                AssetItem.Video(
                    id = dto.id.toString(),
                    title = "Pexels Video ${dto.id}",
                    previewUrl = dto.image,
                    downloadUrl = bestFile?.link ?: "",
                    authorName = dto.user?.name,
                    authorUrl = dto.user?.url,
                    sourceName = serviceName,
                    width = dto.width,
                    height = dto.height,
                    durationSeconds = dto.duration,
                    videoFiles = videoFiles,
                )
            }

        return AssetSearchResult(
            items = items,
            page = response.page,
            perPage = response.perPage,
            totalResults = response.totalResults,
            hasNextPage = response.nextPage != null,
        )
    }
}
