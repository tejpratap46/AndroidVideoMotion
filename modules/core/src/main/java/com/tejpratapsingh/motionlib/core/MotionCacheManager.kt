package com.tejpratapsingh.motionlib.core

import android.net.Uri

/**
 * Interface for managing cached assets.
 */
interface MotionCacheManager {
    /**
     * Returns the local URI if the [remoteUri] is cached, otherwise returns null.
     */
    fun getCachedUri(remoteUri: Uri): Uri?

    /**
     * Returns true if the [remoteUri] is cached.
     */
    fun isCached(remoteUri: Uri): Boolean {
        return getCachedUri(remoteUri) != null
    }

    /**
     * Returns all cached assets as a map of remote URL to local path.
     */
    fun getAllCachedAssets(): Map<String, String>

    /**
     * Deletes the cached asset for the given [remoteUrl].
     */
    fun deleteCachedAsset(remoteUrl: String)

    /**
     * Deletes all cached assets.
     */
    fun clearAll()
}
