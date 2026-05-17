package com.tejpratapsingh.motionlib.core.motion.transitions

import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.BaseMotionTransition
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionlib.ui.effects.SlideEffect

enum class SlideDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    TOP_TO_BOTTOM,
    BOTTOM_TO_TOP,
}

class SlideTransition(
    val direction: SlideDirection,
) : BaseMotionTransition() {
    override fun onApply(
        from: MotionView,
        to: MotionView,
        transitionStartFrame: Int,
        transitionEndFrame: Int,
        duration: Int,
    ) {
        val config = provideCurrentConfig()
        val width = config.aspectRatio.width.toFloat()
        val height = config.aspectRatio.height.toFloat()

        when (direction) {
            SlideDirection.LEFT_TO_RIGHT -> {
                from.addEffect(SlideEffect(transitionStartFrame, transitionEndFrame, fromX = 0f, toX = width))
                to.addEffect(SlideEffect(transitionStartFrame, transitionEndFrame, fromX = -width, toX = 0f))
            }

            SlideDirection.RIGHT_TO_LEFT -> {
                from.addEffect(SlideEffect(transitionStartFrame, transitionEndFrame, fromX = 0f, toX = -width))
                to.addEffect(SlideEffect(transitionStartFrame, transitionEndFrame, fromX = width, toX = 0f))
            }

            SlideDirection.TOP_TO_BOTTOM -> {
                from.addEffect(SlideEffect(transitionStartFrame, transitionEndFrame, fromY = 0f, toY = height))
                to.addEffect(SlideEffect(transitionStartFrame, transitionEndFrame, fromY = -height, toY = 0f))
            }

            SlideDirection.BOTTOM_TO_TOP -> {
                from.addEffect(SlideEffect(transitionStartFrame, transitionEndFrame, fromY = 0f, toY = -height))
                to.addEffect(SlideEffect(transitionStartFrame, transitionEndFrame, fromY = height, toY = 0f))
            }
        }
    }
}
