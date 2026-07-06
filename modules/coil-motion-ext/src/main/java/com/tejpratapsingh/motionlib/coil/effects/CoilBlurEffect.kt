package com.tejpratapsingh.motionlib.coil.effects

import android.view.View
import android.widget.ImageView
import coil.size.Size
import com.commit451.coiltransformations.BlurTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.toBitmap

/**
 * A [MotionEffect] that applies a blur transformation using Coil transformations.
 * Note: For better performance on Android 12+, use RenderEffect based BlurEffect.
 */
class CoilBlurEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val radius: Float = 10f,
    val sampling: Float = 1f,
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView

        if (frame !in startFrame..endFrame) {
            return motionView
        }

        val view = motionView as View

        // Coil transformations are designed for Bitmaps.
        // For a MotionEffect to work on a View (like MotionImageView or VideoFrameView),
        // it must be applied to the view's content.
        // Since Coil transformations return a NEW bitmap, we can only easily apply this
        // if the target view is an ImageView and we replace its bitmap,
        // but that would interfere with other effects and the original content.

        // However, if we are in the production pipeline, getViewBitmap() is called.
        // We could potentially intercept getViewBitmap() but MotionEffect doesn't do that.

        // Given the constraints of the current architecture where MotionEffect.forFrame(frame)
        // is called to update the view's state for a frame, and Coil transformations
        // process bitmaps, we will implement this by transforming the view's bitmap
        // if the view is an ImageView.

        if (view is ImageView) {
            val transformation = BlurTransformation(view.context, radius, sampling)
            val originalBitmap = view.toBitmap()
            val blurredBitmap =
                runBlockingSync {
                    transformation.transform(originalBitmap, Size.ORIGINAL)
                }
            view.setImageBitmap(blurredBitmap)
        }

        return motionView
    }
}
