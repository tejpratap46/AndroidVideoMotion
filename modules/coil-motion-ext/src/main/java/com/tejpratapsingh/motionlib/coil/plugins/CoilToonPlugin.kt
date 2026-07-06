package com.tejpratapsingh.motionlib.coil.plugins

import android.content.Context
import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.gpu.ToonFilterTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a toon filter transformation using Coil transformations.
 */
class CoilToonPlugin(
    private val context: Context,
    val threshold: Float = 0.2f,
    val quantizationLevels: Float = 10.0f,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = ToonFilterTransformation(context, threshold, quantizationLevels)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
