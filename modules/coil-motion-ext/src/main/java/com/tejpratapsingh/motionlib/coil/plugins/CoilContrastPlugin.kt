package com.tejpratapsingh.motionlib.coil.plugins

import android.content.Context
import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.gpu.ContrastFilterTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a contrast filter transformation using Coil transformations.
 */
class CoilContrastPlugin(
    private val context: Context,
    val contrast: Float = 1.0f,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = ContrastFilterTransformation(context, contrast)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
