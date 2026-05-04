package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.ui.custom.CutoutTextView
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView

class TransparentTextView(
    context: Context,
    text: String,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
) : AbstractMotionTextView(
        context = context,
        text = text,
        startFrame = startFrame,
        endFrame = endFrame,
        textView = CutoutTextView(context),
        effects = effects,
    ) {
    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)

        textView.text = text

        return this
    }
}
