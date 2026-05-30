package com.tejpratapsingh.motionlib.ui.effects

import android.graphics.RenderEffect
import android.os.Build
import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator

/**
 * A [MotionEffect] that applies a rendering offset using [RenderEffect.createOffsetEffect].
 * This offsets the content without changing the layout bounds.
 */
class OffsetEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val fromOffsetX: Float = 0f,
    val toOffsetX: Float = 0f,
    val fromOffsetY: Float = 0f,
    val toOffsetY: Float = 0f,
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView
        val view = motionView as View

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return motionView

        if (frame !in startFrame..endFrame) {
            if (frame > endFrame) {
                view.setRenderEffect(null)
            }
            return motionView
        }

        val offsetX = MotionInterpolator.interpolateForRange(
            interpolator = Interpolators(Easings.LINEAR),
            currentFrame = frame,
            frameRange = Pair(startFrame, endFrame),
            valueRange = Pair(fromOffsetX, toOffsetX),
        )

        val offsetY = MotionInterpolator.interpolateForRange(
            interpolator = Interpolators(Easings.LINEAR),
            currentFrame = frame,
            frameRange = Pair(startFrame, endFrame),
            valueRange = Pair(fromOffsetY, toOffsetY),
        )

        view.setRenderEffect(RenderEffect.createOffsetEffect(offsetX, offsetY))

        return motionView
    }
}
