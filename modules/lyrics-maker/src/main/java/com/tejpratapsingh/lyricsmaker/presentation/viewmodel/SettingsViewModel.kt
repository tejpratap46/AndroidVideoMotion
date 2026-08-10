package com.tejpratapsingh.lyricsmaker.presentation.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.tejpratapsingh.motionlib.assettype.SimpleMotionAsset
import com.tejpratapsingh.motionlib.core.MotionAssetManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val cacheManager: MotionAssetManager,
) : ViewModel() {
    private val _cachedAssets = MutableStateFlow<Map<String, String>>(emptyMap())
    val cachedAssets: StateFlow<Map<String, String>> = _cachedAssets.asStateFlow()

    init {
        loadCachedAssets()
    }

    fun loadCachedAssets() {
        _cachedAssets.value = cacheManager.getAllCachedAssets()
    }

    fun deleteAsset(url: String) {
        cacheManager.deleteCachedAsset(SimpleMotionAsset(url.toUri()))
        loadCachedAssets()
    }

    fun clearAllAssets() {
        cacheManager.clearAll()
        loadCachedAssets()
    }
}
