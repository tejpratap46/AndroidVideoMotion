package com.tejpratapsingh.motionlib.ui.effects

import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator

class SlideRightToLeftEffect(
    override val motionView: MotionView,
    override val startFrame: Int,
    override val endFrame: Int
) : MotionEffect {
    private val TAG by lazy {
        "SlideRightToLeftEffect"
    }

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) motionView
        if (frame < startFrame || frame > endFrame) motionView

        val view = motionView as View

        val progress = MotionInterpolator.interpolateForRange(
            interpolator = Interpolators(Easings.LINEAR),
            currentFrame = frame,
            frameRange = Pair(startFrame, endFrame),
            valueRange = Pair(0f, 1f)
        )

        val width = view.width.toFloat()

        // Start outside right, move to original position
        view.translationX = width * (1f - progress)

        return motionView
    }
}