package com.tejpratapsingh.motionlib.coil.plugins

import android.content.Context
import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.BlurTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a blur transformation using Coil transformations.
 */
class CoilBlurPlugin(
    private val context: Context,
    val radius: Float = 10f,
    val sampling: Float = 1f,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = BlurTransformation(context, radius, sampling)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
