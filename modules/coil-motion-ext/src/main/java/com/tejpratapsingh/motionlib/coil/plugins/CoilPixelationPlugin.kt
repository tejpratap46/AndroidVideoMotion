package com.tejpratapsingh.motionlib.coil.plugins

import android.content.Context
import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.gpu.PixelationFilterTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a pixelation filter transformation using Coil transformations.
 */
class CoilPixelationPlugin(
    private val context: Context,
    val pixel: Float = 10f,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = PixelationFilterTransformation(context, pixel)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
