package com.tejpratapsingh.lyricsmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.tejpratapsingh.motionlib.core.MotionCacheManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val cacheManager: MotionCacheManager
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
        cacheManager.deleteCachedAsset(url)
        loadCachedAssets()
    }

    fun clearAllAssets() {
        cacheManager.clearAll()
        loadCachedAssets()
    }
}
