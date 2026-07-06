package com.tejpratapsingh.motionlib.coil.plugins

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import coil.size.Size
import com.commit451.coiltransformations.gpu.VignetteFilterTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a vignette filter transformation using Coil transformations.
 */
class CoilVignettePlugin(
    private val context: Context,
    val center: PointF = PointF(0.5f, 0.5f),
    val color: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f),
    val start: Float = 0.0f,
    val end: Float = 0.75f,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = VignetteFilterTransformation(context, center, color, start, end)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
