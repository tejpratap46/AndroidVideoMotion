package com.tejpratapsingh.motionlib.coil.plugins

import android.content.Context
import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.gpu.SepiaFilterTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a sepia filter transformation using Coil transformations.
 */
class CoilSepiaPlugin(
    private val context: Context,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = SepiaFilterTransformation(context)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
