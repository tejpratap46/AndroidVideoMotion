package com.tejpratapsingh.motionlib.ui.custom.text.abstract

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import com.tejpratapsingh.motionlib.utils.getWebFont

abstract class AbstractMotionTextView(
    context: Context,
    text: String,
    startFrame: Int,
    endFrame: Int,
    val textView: AppCompatTextView,
    writingSpeed: Float = 1f,
    fontUrl: String? = null,
) : BaseContourMotionView(context, startFrame, endFrame) {
    protected val inferredEndFrame: Int =
        if (endFrame != -1 && writingSpeed > 0) {
            (startFrame + (endFrame - startFrame) / writingSpeed).toInt()
        } else {
            endFrame
        }

    init {
        textView.apply {
            if (fontUrl != null) {
                typeface = getWebFont(fontUrl)
            }
        }

        textView.layoutBy(
            x =
                leftTo {
                    parent.left()
                }.rightTo {
                    parent.right()
                },
            y =
                topTo {
                    parent.top()
                }.bottomTo {
                    parent.bottom()
                },
        )
        textView.text = text
    }
}
