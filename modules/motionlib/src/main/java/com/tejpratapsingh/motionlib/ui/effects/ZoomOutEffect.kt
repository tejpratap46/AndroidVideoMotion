package com.tejpratapsingh.motionlib.ui.effects

import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator

class ZoomOutEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val startScale: Float = 2f,
    val endScale: Float = 1f,
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView
        if (frame !in startFrame..endFrame) return motionView

        val view = motionView as View

        val scale =
            MotionInterpolator.interpolateForRange(
                interpolator = Interpolators(Easings.LINEAR),
                currentFrame = frame,
                frameRange = Pair(startFrame, endFrame),
                valueRange = Pair(startScale, endScale),
            )

        view.scaleX = scale
        view.scaleY = scale

        return motionView
    }
}
