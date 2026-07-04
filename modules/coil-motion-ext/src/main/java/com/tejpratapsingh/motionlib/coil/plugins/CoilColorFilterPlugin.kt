package com.tejpratapsingh.motionlib.coil.plugins

import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.ColorFilterTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a color filter transformation using Coil transformations.
 */
class CoilColorFilterPlugin(
    val color: Int,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = ColorFilterTransformation(color)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
