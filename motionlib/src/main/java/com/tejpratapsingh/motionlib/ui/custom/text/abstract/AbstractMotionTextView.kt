package com.tejpratapsingh.motionlib.ui.custom.text.abstract

import android.content.Context
import android.widget.TextView
import com.tejpratapsingh.motionlib.core.motion.MotionView

abstract class AbstractMotionTextView(
    context: Context,
    text: String,
    startFrame: Int,
    endFrame: Int
) :
    MotionView(context, startFrame, endFrame) {
    val textView: TextView = TextView(context)

    init {
        textView.layoutBy(
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
        textView.text = text
    }
    }