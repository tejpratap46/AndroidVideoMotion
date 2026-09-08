package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView

class WordBlinkTextView(
    context: Context,
    text: String,
    startFrame: Int = 0,
    endFrame: Int = -1,
    writingSpeed: Float = 0f,
    textView: AppCompatTextView = AppCompatTextView(context),
    fontAsset: MotionAsset? = null,
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
) : AbstractMotionTextView(
        context = context,
        text = text,
        startFrame = startFrame,
        endFrame = endFrame,
        textView = textView,
        writingSpeed = writingSpeed,
        fontAsset = fontAsset,
        textSizeVariant = textSizeVariant,
        textColor = textColor,
        effects = effects,
    ) {
    init {
        textView.maxLines = 1

        textView.gravity = Gravity.CENTER
    }

    private val wordArray = text.split(" ")
    private val wordCount: Int = wordArray.size

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            textView,
            12,
            100,
            1,
            TypedValue.COMPLEX_UNIT_SP,
        )

        if ((inferredEndFrame != -1) && (frame >= inferredEndFrame)) {
            textView.text = text
        } else {
            val progress: Float =
                MotionInterpolator
                    .interpolateForRange(
                        Interpolators(Easings.LINEAR),
                        frame,
                        Pair(startFrame, inferredEndFrame),
                        Pair(0f, wordCount.toFloat()),
                    )

            if (wordCount > 0) {
                val wordIndex = progress.toInt().coerceIn(0, wordCount - 1)
                textView.text = wordArray[wordIndex]
            } else {
                textView.text = text
            }
        }

        textView.invalidate()

        return this
    }
}
