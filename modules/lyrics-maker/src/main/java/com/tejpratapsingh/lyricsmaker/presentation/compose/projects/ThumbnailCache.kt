package com.tejpratapsingh.lyricsmaker.presentation.compose.projects

import android.graphics.Bitmap
import android.util.LruCache
import java.util.*

object ThumbnailCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8 // Use 1/8th of available memory

    private val cache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    fun get(key: String): Bitmap? = cache.get(key)

    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }

    fun remove(key: String) {
        cache.remove(key)
    }

    fun removeByPrefix(prefix: String) {
        val keysToRemove = mutableListOf<String>()
        cache.snapshot().keys.forEach {
            if (it.startsWith(prefix)) {
                keysToRemove.add(it)
            }
        }
        keysToRemove.forEach { cache.remove(it) }
    }

    fun clear() {
        cache.evictAll()
    }
}
