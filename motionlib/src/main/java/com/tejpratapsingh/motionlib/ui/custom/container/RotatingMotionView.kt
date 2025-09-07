package com.tejpratapsingh.motionlib.ui.custom.container

import android.content.Context
import android.view.View
import com.tejpratapsingh.motionlib.core.motion.BaseMotionView

class RotatingMotionView(
    context: Context,
    startFrame: Int,
    endFrame: Int,
    view: View,
    private val degreePerSecond: Float = 6f
) : BaseMotionView(context, startFrame, endFrame) {

    init {
        view.layoutBy(
            x = leftTo {
                parent.left()
            }.rightTo {
                parent.right()
            }, y = topTo {
                parent.top()
            }.bottomTo {
                parent.bottom()
            }
        )
    }

    override fun forFrame(frame: Int): BaseMotionView {
        val totalFrames = endFrame - startFrame + 1
        val durationSeconds = totalFrames / motionConfig.fps.toFloat()
        val totalRotation = degreePerSecond * durationSeconds
        val rotationPerFrame = totalRotation / totalFrames
        val currentRotation = (frame - startFrame) * rotationPerFrame

        this.rotation = currentRotation

        return this
    }
}