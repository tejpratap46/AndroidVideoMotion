package com.tejpratapsingh.motionlib.coil.plugins

import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.CropTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a crop transformation using Coil transformations.
 */
class CoilCropPlugin(
    val type: CropTransformation.CropType = CropTransformation.CropType.CENTER,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = CropTransformation(type)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
