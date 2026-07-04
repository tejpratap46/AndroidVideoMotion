package com.tejpratapsingh.motionlib.coil.plugins

import android.content.Context
import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.MaskTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a mask transformation using Coil transformations.
 */
class CoilMaskPlugin(
    private val context: Context,
    val maskId: Int,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = MaskTransformation(context, maskId)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
