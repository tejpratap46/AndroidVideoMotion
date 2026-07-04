package com.tejpratapsingh.motionlib.coil.plugins

import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.facedetection.CenterOnFaceTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a center-on-face transformation using Coil transformations.
 */
class CoilCenterOnFacePlugin(
    val zoom: Int = 100,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = CenterOnFaceTransformation(zoom = zoom)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
