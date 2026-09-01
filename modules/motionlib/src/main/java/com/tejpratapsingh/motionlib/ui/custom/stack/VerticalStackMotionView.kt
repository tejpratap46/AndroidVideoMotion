package com.tejpratapsingh.motionlib.ui.custom.stack

import android.content.Context
import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView

/**
 * A [MotionView] that stacks its children vertically.
 * Each child is given a percentage of the total height.
 */
open class VerticalStackMotionView(
    context: Context,
    startFrame: Int,
    endFrame: Int,
    val sections: List<StackSection>,
    effects: List<MotionEffect> = emptyList(),
) : BaseContourMotionView(context, startFrame, endFrame, effects = effects) {
    private var isStackInitialized = false

    private fun initializeStack() {
        if (isStackInitialized) return
        var currentPercent = 0f
        val videoAspectRatio = motionConfig.aspectRatio
        for (section in sections) {
            val childMotionView = section.view

            val nextPercent = currentPercent + section.percentage
            val startPercent = currentPercent / 100f
            val endPercent = nextPercent / 100f

            val sectionHeight = (videoAspectRatio.height * (section.percentage / 100f)).toInt()
            childMotionView.layoutInfo =
                childMotionView.layoutInfo.copy(
                    width = videoAspectRatio.width,
                    height = sectionHeight,
                )

            val childView = section.view as View
            childView.layoutBy(
                x = leftTo { parent.left() }.rightTo { parent.right() },
                y =
                    topTo {
                        val parentHeight = parent.height()
                        parent.top() + (parentHeight * startPercent).toInt()
                    }.bottomTo {
                        val parentHeight = parent.height()
                        parent.top() + (parentHeight * endPercent).toInt()
                    },
            )
            currentPercent = nextPercent
        }
        isStackInitialized = true
    }

    override fun forFrame(frame: Int): MotionView {
        initializeStack()
        return super.forFrame(frame)
    }
}
