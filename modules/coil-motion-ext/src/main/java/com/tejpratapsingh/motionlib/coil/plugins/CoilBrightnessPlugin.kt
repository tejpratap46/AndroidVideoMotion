package com.tejpratapsingh.motionlib.coil.plugins

import android.content.Context
import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.gpu.BrightnessFilterTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a brightness filter transformation using Coil transformations.
 */
class CoilBrightnessPlugin(
    private val context: Context,
    val brightness: Float = 0.0f,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = BrightnessFilterTransformation(context, brightness)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
