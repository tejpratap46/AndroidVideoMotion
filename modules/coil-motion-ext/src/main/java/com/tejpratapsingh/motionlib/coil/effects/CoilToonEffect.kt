package com.tejpratapsingh.motionlib.coil.effects

import android.view.View
import android.widget.ImageView
import coil.size.Size
import com.commit451.coiltransformations.gpu.ToonFilterTransformation
import com.tejpratapsingh.motionlib.coil.utils.runBlockingSync
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.toBitmap

/**
 * A [MotionEffect] that applies a toon filter transformation using Coil transformations.
 */
class CoilToonEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val threshold: Float = 0.2f,
    val quantizationLevels: Float = 10.0f,
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView
        val view = motionView as View

        if (frame !in startFrame..endFrame) {
            return motionView
        }

        if (view is ImageView) {
            val transformation = ToonFilterTransformation(view.context, threshold, quantizationLevels)
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
