package com.tejpratapsingh.motionlib.coil.effects

import android.view.View
import android.widget.ImageView
import coil.size.Size
import com.commit451.coiltransformations.GrayscaleTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.toBitmap

/**
 * A [MotionEffect] that applies a grayscale transformation using Coil transformations.
 */
class CoilGrayscaleEffect(
    override val startFrame: Int,
    override val endFrame: Int,
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView

        if (frame !in startFrame..endFrame) {
            return motionView
        }

        val view = motionView as View
        if (view is ImageView) {
            val transformation = GrayscaleTransformation()
            val originalBitmap = view.toBitmap()
            val grayscaleBitmap =
                runBlockingSync {
                    transformation.transform(originalBitmap, Size.ORIGINAL)
                }
            view.setImageBitmap(grayscaleBitmap)
        }

        return motionView
    }
}
