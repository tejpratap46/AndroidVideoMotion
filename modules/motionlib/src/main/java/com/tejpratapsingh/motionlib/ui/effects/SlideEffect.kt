package com.tejpratapsingh.motionlib.ui.effects

import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator

class SlideEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val fromX: Float? = null,
    val toX: Float? = null,
    val fromY: Float? = null,
    val toY: Float? = null,
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView

        val view = motionView as View

        if (frame !in startFrame..endFrame) {
            // Reset translation if outside range
            // This might be tricky if multiple slide effects are used,
            // but usually we want it to stay at final position if it's the end of view.
            return motionView
        }

        val progress =
            MotionInterpolator.interpolateForRange(
                interpolator = Interpolators(Easings.LINEAR),
                currentFrame = frame,
                frameRange = Pair(startFrame, endFrame),
                valueRange = Pair(0f, 1f),
            )

        if (fromX != null && toX != null) {
            view.translationX = fromX + (toX - fromX) * progress
        }

        if (fromY != null && toY != null) {
            view.translationY = fromY + (toY - fromY) * progress
        }

        return motionView
    }
}
