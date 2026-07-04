package com.tejpratapsingh.motionlib.coil.effects

import android.graphics.PointF
import android.view.View
import android.widget.ImageView
import coil.size.Size
import com.commit451.coiltransformations.gpu.SwirlFilterTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.toBitmap

/**
 * A [MotionEffect] that applies a swirl filter transformation using Coil transformations.
 */
class CoilSwirlEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val radius: Float = 0.5f,
    val angle: Float = 1.0f,
    val center: PointF = PointF(0.5f, 0.5f),
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView
        val view = motionView as View

        if (frame !in startFrame..endFrame) {
            return motionView
        }

        if (view is ImageView) {
            val transformation = SwirlFilterTransformation(view.context, radius, angle, center)
            val originalBitmap = view.toBitmap()
            val filteredBitmap =
                runBlockingSync {
                    transformation.transform(originalBitmap, Size.ORIGINAL)
                }
            view.setImageBitmap(filteredBitmap)
        }

        return motionView
    }
}
