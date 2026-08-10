package com.tejpratapsingh.motionlib.core

import android.net.Uri

/**
 * Interface for managing cached assets.
 */
interface MotionAssetManager {
    /**
     * Returns the local URI if the [asset] is cached, otherwise returns null.
     */
    fun getCachedUri(asset: MotionAsset): Uri?

    /**
     * Returns the local path if the [asset] is cached in the central store, otherwise returns null.
     */
    fun getLocalPath(asset: MotionAsset): String?

    /**
     * Returns true if the [asset] is cached.
     */
    fun isCached(asset: MotionAsset): Boolean = getCachedUri(asset) != null

    /**
     * Returns all cached assets as a map of remote URL to local path.
     */
    fun getAllCachedAssets(): Map<String, String>

    /**
     * Deletes the cached asset for the given [asset].
     */
    fun deleteCachedAsset(asset: MotionAsset)

    /**
     * Deletes all cached assets.
     */
    fun clearAll()
}
