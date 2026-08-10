package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView

class TransparentTextView(
    context: Context,
    text: String,
    startFrame: Int,
    endFrame: Int,
    fontAsset: MotionAsset? = null,
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
) : AbstractMotionTextView(
        context = context,
        text = text,
        startFrame = startFrame,
        endFrame = endFrame,
        textView = AppCompatTextView(context),
        fontAsset = fontAsset,
        textSizeVariant = textSizeVariant,
        textColor = textColor,
        effects = effects,
    ) {
    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)

        textView.text = text

        return this
    }
}
