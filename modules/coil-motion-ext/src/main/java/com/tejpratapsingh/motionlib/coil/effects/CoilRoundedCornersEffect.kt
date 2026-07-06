package com.tejpratapsingh.motionlib.coil.effects

import android.view.View
import android.widget.ImageView
import coil.size.Size
import coil.transform.RoundedCornersTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.toBitmap

/**
 * A [MotionEffect] that applies a rounded corners transformation using Coil transformations.
 */
class CoilRoundedCornersEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val radius: Float,
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView

        if (frame !in startFrame..endFrame) {
            return motionView
        }

        val view = motionView as View
        if (view is ImageView) {
            val transformation = RoundedCornersTransformation(radius)
            val originalBitmap = view.toBitmap()
            val roundedBitmap =
                runBlockingSync {
                    transformation.transform(originalBitmap, Size.ORIGINAL)
                }
            view.setImageBitmap(roundedBitmap)
        }

        return motionView
    }
}
