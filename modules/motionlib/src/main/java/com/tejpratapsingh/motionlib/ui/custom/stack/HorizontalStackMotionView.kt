package com.tejpratapsingh.motionlib.ui.custom.stack

import android.content.Context
import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView

/**
 * A [MotionView] that stacks its children horizontally.
 * Each child is given a percentage of the total width.
 */
open class HorizontalStackMotionView(
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

            val sectionWidth = (videoAspectRatio.width * (section.percentage / 100f)).toInt()
            childMotionView.layoutInfo =
                childMotionView.layoutInfo.copy(
                    width = sectionWidth,
                    height = videoAspectRatio.height,
                )

            val childView = section.view as View
            childView.layoutBy(
                x =
                    leftTo { parent.left() + (parent.width() * startPercent).toInt() }
                        .rightTo { parent.left() + (parent.width() * endPercent).toInt() },
                y = topTo { parent.top() }.bottomTo { parent.bottom() },
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
