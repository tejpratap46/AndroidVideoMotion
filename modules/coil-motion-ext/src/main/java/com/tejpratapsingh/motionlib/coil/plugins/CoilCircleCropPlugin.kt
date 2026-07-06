package com.tejpratapsingh.motionlib.coil.plugins

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.CircleCropTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a circle crop transformation using Coil transformations.
 */
class CoilCircleCropPlugin : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = CircleCropTransformation()
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
