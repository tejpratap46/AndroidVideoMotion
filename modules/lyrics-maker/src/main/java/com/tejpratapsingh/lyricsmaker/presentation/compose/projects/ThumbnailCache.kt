package com.tejpratapsingh.lyricsmaker.presentation.compose.projects

import android.graphics.Bitmap
import android.util.LruCache

object ThumbnailCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8 // Use 1/8th of available memory

    private val cache =
        object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(
                key: String,
                value: Bitmap,
            ): Int = value.byteCount / 1024
        }

    fun get(key: String): Bitmap? = cache.get(key)

    fun put(
        key: String,
        bitmap: Bitmap,
    ) {
        val scaled =
            if (bitmap.width > 300 || bitmap.height > 300) {
                val scale = 300f / maxOf(bitmap.width, bitmap.height)
                val newW = (bitmap.width * scale).toInt().coerceAtLeast(1)
                val newH = (bitmap.height * scale).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(bitmap, newW, newH, true)
            } else {
                bitmap
            }
        cache.put(key, scaled)
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
