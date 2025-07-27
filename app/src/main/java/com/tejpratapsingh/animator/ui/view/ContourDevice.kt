package com.tejpratapsingh.animator.ui.view

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.motion.MotionView
import com.tejpratapsingh.motionlib.ui.custom.text.TypeWriterTextView

class ContourDevice(context: Context, startFrame: Int, endFrame: Int) :
    MotionView(context, startFrame, endFrame) {

    private val typeWriterWriterTextView: TypeWriterTextView = TypeWriterTextView(
        context = context,
        text = "Hello,\nWelcome to the future",
        startFrame = startFrame,
        endFrame = endFrame
    ).apply {
        setBackgroundColor(Color.WHITE)

        textView.textSize = 18f
        textView.gravity = Gravity.CENTER
    }

    init {
        typeWriterWriterTextView.layoutBy(
            x = leftTo {
                parent.left()
            }.rightTo {
                parent.right()
            },
            y = topTo {
                parent.top()
            }.bottomTo {
                parent.bottom()
            }
        )

        contourHeightOf {
            motionConfig.height.toYInt()
        }
        contourWidthOf {
            motionConfig.width.toXInt()
        }
    }

    override fun forFrame(frame: Int): View {
        super.forFrame(frame)

        val backgroundColor: Int = MotionInterpolator.interpolateColorForRange(
            Interpolators(Easings.LINEAR),
            frame,
            Pair(startFrame, endFrame),
            Pair("#2568ff".toColorInt(), "#ba28ff".toColorInt())
        )

        typeWriterWriterTextView.setBackgroundColor(
            backgroundColor
        )

        typeWriterWriterTextView.textView.setTextColor(
            MotionInterpolator.getComplementaryColor(
                backgroundColor
            )
        )

        return this
    }
}