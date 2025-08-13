package com.tejpratapsingh.motionlib.ui.custom.text.abstract

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.motionlib.core.motion.BaseMotionView
import com.tejpratapsingh.motionlib.utils.getWebFont

abstract class AbstractMotionTextView(
    context: Context,
    text: String,
    startFrame: Int,
    endFrame: Int,
    val textView: AppCompatTextView,
    font: String? = null
) : BaseMotionView(context, startFrame, endFrame) {

    init {
        textView.apply {
            if (font != null) {
                typeface = getWebFont(font)
            }
        }

        textView.layoutBy(x = leftTo {
            parent.left()
        }.rightTo {
            parent.right()
        }, y = topTo {
            parent.top()
        }.bottomTo {
            parent.bottom()
        })
        textView.text = text
    }
}