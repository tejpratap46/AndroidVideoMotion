package com.tejpratapsingh.motionlib.core.motion.transitions

import android.view.ViewGroup
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.BaseMotionTransition
import com.tejpratapsingh.motionlib.ui.effects.FadeInEffect
import com.tejpratapsingh.motionlib.ui.effects.FadeOutEffect

class CrossFadeTransition : BaseMotionTransition() {
    override fun onApply(
        from: MotionView,
        to: MotionView,
        transitionStartFrame: Int,
        transitionEndFrame: Int,
        duration: Int
    ) {
        from.addEffect(FadeOutEffect(transitionStartFrame, transitionEndFrame))
        to.addEffect(FadeInEffect(transitionStartFrame, transitionEndFrame))
    }
}
