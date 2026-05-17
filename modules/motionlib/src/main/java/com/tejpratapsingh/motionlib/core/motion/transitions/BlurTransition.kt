package com.tejpratapsingh.motionlib.core.motion.transitions

import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.BaseMotionTransition
import com.tejpratapsingh.motionlib.ui.effects.BlurEffect

class BlurTransition(
    val maxBlurRadius: Float = 20f,
) : BaseMotionTransition() {
    override fun onApply(
        from: MotionView,
        to: MotionView,
        transitionStartFrame: Int,
        transitionEndFrame: Int,
        duration: Int,
    ) {
        from.addEffect(
            BlurEffect(
                startFrame = transitionStartFrame,
                endFrame = transitionEndFrame,
                fromBlurRadius = 0.1f,
                toBlurRadius = maxBlurRadius,
            ),
        )
        to.addEffect(
            BlurEffect(
                startFrame = transitionStartFrame,
                endFrame = transitionEndFrame,
                fromBlurRadius = maxBlurRadius,
                toBlurRadius = 0.1f,
            ),
        )
    }
}
