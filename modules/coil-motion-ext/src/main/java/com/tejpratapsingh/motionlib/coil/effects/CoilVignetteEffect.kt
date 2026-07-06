package com.tejpratapsingh.motionlib.coil.effects

import android.graphics.PointF
import android.view.View
import android.widget.ImageView
import coil.size.Size
import com.commit451.coiltransformations.gpu.VignetteFilterTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.toBitmap

/**
 * A [MotionEffect] that applies a vignette filter transformation using Coil transformations.
 */
class CoilVignetteEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val center: PointF = PointF(0.5f, 0.5f),
    val color: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f),
    val start: Float = 0.0f,
    val end: Float = 0.75f,
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView
        val view = motionView as View

        if (frame !in startFrame..endFrame) {
            return motionView
        }

        if (view is ImageView) {
            val transformation = VignetteFilterTransformation(view.context, center, color, start, end)
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
