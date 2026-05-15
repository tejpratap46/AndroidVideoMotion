package com.tejpratapsingh.motionlib.core.motion

import android.view.ViewGroup
import com.tejpratapsingh.motionlib.core.MotionTransition
import com.tejpratapsingh.motionlib.core.MotionView

/**
 * Base class for [MotionTransition] that handles frame range adjustments for overlap.
 */
abstract class BaseMotionTransition : MotionTransition {
    override fun apply(
        from: MotionView,
        to: MotionView,
        duration: Int,
    ) {
        val halfDuration = duration / 2
        
        // Transition is centered around the boundary (to.startFrame)
        val boundary = to.startFrame
        val transitionStart = boundary - halfDuration
        val transitionEnd = transitionStart + duration - 1
        
        onApply(from, to, transitionStart, transitionEnd, duration)
    }

    abstract fun onApply(
        from: MotionView,
        to: MotionView,
        transitionStartFrame: Int,
        transitionEndFrame: Int,
        duration: Int,
    )
}
