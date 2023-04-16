package com.tejpratapsingh.animator.ui.view

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.ui.custom.text.WordBlinkTextView
import com.tejpratapsingh.motionlib.utils.Easings
import com.tejpratapsingh.motionlib.utils.Interpolators
import com.tejpratapsingh.motionlib.utils.MotionInterpolator

class ContourDevice(context: Context, startFrame: Int, endFrame: Int) :
    MotionView(context, startFrame, endFrame) {

    private val typeWriterWriterTextView: WordBlinkTextView = WordBlinkTextView(
        context = context,
        text = "If you define a custom setter, it will be called every time you assign a value to the property, except its initialization. A custom setter looks like this:",
        startFrame = startFrame,
        endFrame = endFrame
    ).apply {
        setBackgroundColor(Color.WHITE)

        textView.textSize = 18f
        textView.gravity = Gravity.CENTER
    }

//    val typeWriterTextView: TextView = TextView(context).apply {
//        textSize = 48f
//        gravity = Gravity.CENTER
//    }

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
            Pair(Color.parseColor("#2568ff"), Color.parseColor("#ba28ff"))
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