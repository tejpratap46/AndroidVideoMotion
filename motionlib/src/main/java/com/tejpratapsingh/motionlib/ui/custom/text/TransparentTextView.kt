package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.ui.custom.CutoutTextView
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView

class TransparentTextView(
    context: Context, private val text: String, startFrame: Int, endFrame: Int
) : AbstractMotionTextView(
    context,
    text,
    startFrame,
    endFrame,
    textView = CutoutTextView(context)
) {
    private val TAG by lazy {
        "TransparentTextView"
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)

        textView.text = text

        return this
    }
}