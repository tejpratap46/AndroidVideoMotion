package com.tejpratapsingh.motionlib.coil.plugins

import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.GrayscaleTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a grayscale transformation using Coil transformations.
 */
class CoilGrayscalePlugin : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = GrayscaleTransformation()
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
