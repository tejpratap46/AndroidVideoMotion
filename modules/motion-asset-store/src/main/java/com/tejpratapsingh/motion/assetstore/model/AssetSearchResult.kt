package com.tejpratapsingh.motion.assetstore.model

data class AssetSearchResult<out T : AssetItem>(
    val items: List<T>,
    val page: Int,
    val perPage: Int,
    val totalResults: Int,
    val hasNextPage: Boolean,
)
