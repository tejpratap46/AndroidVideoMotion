package com.tejpratapsingh.motionlib.coil.plugins

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import coil.size.Size
import com.commit451.coiltransformations.gpu.SwirlFilterTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a swirl filter transformation using Coil transformations.
 */
class CoilSwirlPlugin(
    private val context: Context,
    val radius: Float = 0.5f,
    val angle: Float = 1.0f,
    val center: PointF = PointF(0.5f, 0.5f),
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = SwirlFilterTransformation(context, radius, angle, center)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
