package com.tejpratapsingh.motionlib.ui.effects

import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator

class FadeInEffect(
    override val startFrame: Int,
    override val endFrame: Int,
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView

        val view = motionView as View

        if (frame !in startFrame..endFrame) {
            // If we are past the effect, ensure alpha is 1
            if (frame > endFrame) {
                view.alpha = 1f
            }
            return motionView
        }

        val alpha =
            MotionInterpolator.interpolateForRange(
                interpolator = Interpolators(Easings.LINEAR),
                currentFrame = frame,
                frameRange = Pair(startFrame, endFrame),
                valueRange = Pair(0f, 1f),
            )

        view.alpha = alpha

        return motionView
    }
}
