package com.tejpratapsingh.motionlib.assettype

import android.content.Context
import android.net.Uri
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionAsset

/**
 * Implementation of [MotionAsset] for fonts.
 */
class FontAsset(
    private val uri: Uri,
    val fontName: String? = null,
    private val metadata: JsonObject? = null,
) : MotionAsset {
    override fun getUri(): Uri = uri

    override fun getMetadata(): JsonObject? = metadata

    override suspend fun prepare(context: Context): Boolean = true
}
