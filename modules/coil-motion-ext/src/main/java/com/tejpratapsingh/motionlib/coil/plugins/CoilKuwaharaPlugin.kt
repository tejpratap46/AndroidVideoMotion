package com.tejpratapsingh.motionlib.coil.plugins

import android.content.Context
import android.graphics.Bitmap
import coil.size.Size
import com.commit451.coiltransformations.gpu.KuwaharaFilterTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * A [MotionPlugin] that applies a Kuwahara filter transformation using Coil transformations.
 */
class CoilKuwaharaPlugin(
    private val context: Context,
    val radius: Int = 25,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val transformation = KuwaharaFilterTransformation(context, radius)
        return runBlockingSync {
            transformation.transform(input, Size.ORIGINAL)
        }
    }
}
