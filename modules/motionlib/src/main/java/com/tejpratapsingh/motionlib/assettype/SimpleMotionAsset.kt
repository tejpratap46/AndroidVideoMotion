package com.tejpratapsingh.motionlib.assettype

import android.content.Context
import android.net.Uri
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionAssetManager

/**
 * A simple implementation of [MotionAsset] that wraps a [Uri].
 */
class SimpleMotionAsset(
    private val uri: Uri,
    private val metadata: JsonObject? = null,
) : MotionAsset {
    override fun getUri(): Uri = uri

    override fun getMetadata(): JsonObject? = metadata

    override suspend fun prepare(context: Context): Boolean = true

    override fun isPrepared(
        context: Context,
        cacheManager: MotionAssetManager,
    ): Boolean = true
}
