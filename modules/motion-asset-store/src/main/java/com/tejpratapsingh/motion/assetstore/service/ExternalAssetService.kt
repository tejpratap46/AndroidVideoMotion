package com.tejpratapsingh.motion.assetstore.service

import com.tejpratapsingh.motion.assetstore.model.AssetFileType
import com.tejpratapsingh.motion.assetstore.model.AssetItem
import com.tejpratapsingh.motion.assetstore.model.AssetSearchResult

interface ExternalAssetService {
    val serviceId: String
    val serviceName: String
    val supportedTypes: Set<AssetFileType>

    suspend fun searchImages(
        query: String,
        page: Int = 1,
        perPage: Int = 30,
    ): Result<AssetSearchResult<AssetItem.Image>>

    suspend fun searchVideos(
        query: String,
        page: Int = 1,
        perPage: Int = 30,
    ): Result<AssetSearchResult<AssetItem.Video>>

    suspend fun getCuratedImages(
        page: Int = 1,
        perPage: Int = 30,
    ): Result<AssetSearchResult<AssetItem.Image>>

    suspend fun getPopularVideos(
        page: Int = 1,
        perPage: Int = 30,
    ): Result<AssetSearchResult<AssetItem.Video>>
}
