package com.tejpratapsingh.motionlib.coil.plugins

import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.SquareCropTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a square crop transformation using Coil transformations.
 */
class CoilSquareCropPlugin : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = SquareCropTransformation()
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
