package com.tejpratapsingh.motionlib.coil.plugins

import android.content.Context
import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.gpu.SketchFilterTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a sketch filter transformation using Coil transformations.
 */
class CoilSketchPlugin(
    private val context: Context,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = SketchFilterTransformation(context)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
