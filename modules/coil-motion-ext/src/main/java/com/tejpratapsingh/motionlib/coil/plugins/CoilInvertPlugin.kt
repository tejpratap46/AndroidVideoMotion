package com.tejpratapsingh.motionlib.coil.plugins

import android.content.Context
import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.gpu.InvertFilterTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies an invert filter transformation using Coil transformations.
 */
class CoilInvertPlugin(
    private val context: Context,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = InvertFilterTransformation(context)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
