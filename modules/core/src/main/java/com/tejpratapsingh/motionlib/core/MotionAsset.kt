package com.tejpratapsingh.motionlib.core

import android.content.Context
import android.net.Uri
import com.google.gson.JsonObject

/**
 * Interface representing an asset used in motion video generation.
 * This can be an image, video, audio, or any other resource.
 *
 * It is responsible for providing the URI of the asset and preparing it if required.
 */
interface MotionAsset {
    /**
     * Returns the URI for the asset.
     */
    fun getUri(): Uri

    /**
     * Returns the custom metadata for the asset.
     */
    fun getMetadata(): JsonObject? = null

    /**
     * Prepares/Generates the asset if required.
     * This might involve downloading, generating, or searching for the asset.
     *
     * @param context Android context
     * @return true if preparation was successful, false otherwise.
     */
    suspend fun prepare(context: Context): Boolean

    /**
     * Returns true if the asset is ready for use (e.g. exists in cache).
     *
     * @param cacheManager Cache manager to check for cached assets
     */
    fun isCached(cacheManager: MotionAssetManager): Boolean = cacheManager.isCached(this)

    /**
     * Returns true if the asset is prepared and ready for use.
     * If false, [prepare] must be called.
     */
    fun isPrepared(
        context: Context,
        cacheManager: MotionAssetManager,
    ): Boolean = isCached(cacheManager)
}
