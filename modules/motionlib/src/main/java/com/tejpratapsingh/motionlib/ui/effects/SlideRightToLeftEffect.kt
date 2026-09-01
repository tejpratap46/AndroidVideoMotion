package com.tejpratapsingh.motionlib.ui.effects

import android.view.View
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.findConfig

class SlideRightToLeftEffect(
    override val startFrame: Int,
    override val endFrame: Int,
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView
        if (frame !in startFrame..endFrame) return motionView

        val view = motionView as View

        val motionConfig: MotionConfig = findConfig()

        val progress =
            MotionInterpolator.interpolateForRange(
                interpolator = Interpolators(Easings.LINEAR),
                currentFrame = frame,
                frameRange = Pair(startFrame, endFrame),
                valueRange = Pair(0f, 1f),
            )

        val width = motionConfig.aspectRatio.width.toFloat()

        // Start at original position (0), move to the left (-width)
        view.translationX = -width * progress

        return motionView
    }
}
