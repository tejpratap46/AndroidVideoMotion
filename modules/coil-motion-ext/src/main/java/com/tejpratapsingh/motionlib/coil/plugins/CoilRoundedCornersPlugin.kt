package com.tejpratapsingh.motionlib.coil.plugins

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.RoundedCornersTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a rounded corners transformation using Coil transformations.
 */
class CoilRoundedCornersPlugin(
    val radius: Float,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = RoundedCornersTransformation(radius)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
