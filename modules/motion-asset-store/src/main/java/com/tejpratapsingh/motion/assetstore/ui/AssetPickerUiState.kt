package com.tejpratapsingh.motion.assetstore.ui

import com.tejpratapsingh.motion.assetstore.model.AssetItem

sealed interface AssetPickerUiState<out T : AssetItem> {
    object Loading : AssetPickerUiState<Nothing>

    data class Success<out T : AssetItem>(
        val items: List<T>,
        val page: Int,
        val totalResults: Int,
        val hasNextPage: Boolean,
        val isLoadingMore: Boolean = false,
    ) : AssetPickerUiState<T>

    data class Error(val message: String) : AssetPickerUiState<Nothing>
}
