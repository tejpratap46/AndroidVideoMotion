package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView
import timber.log.Timber

class WordWriterTextView(
    context: Context,
    text: String,
    startFrame: Int = 0,
    endFrame: Int = -1,
    writingSpeed: Float = 0f,
    val unwrittenTextAlpha: Float = 0f,
    textView: AppCompatTextView = AppCompatTextView(context),
    fontAsset: MotionAsset? = null,
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    highlightColor: String? = null,
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
        highlightColor = highlightColor,
        effects = effects,
    ) {
    private val wordArray = text.split(" ")
    private val wordCount: Int = wordArray.size

    init {
        contourHeightOf {
            provideCurrentConfig()
                .aspectRatio.height
                .toYInt()
        }
        contourWidthOf {
            provideCurrentConfig()
                .aspectRatio.width
                .toXInt()
        }

        textView.gravity = Gravity.CENTER
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        Timber.d("start = $startFrame, end = $endFrame, frame = $frame")

        val visibleWordCount: Int =
            MotionInterpolator
                .interpolateForRange(
                    Interpolators(Easings.LINEAR),
                    frame,
                    Pair(startFrame, inferredEndFrame),
                    Pair(0f, wordCount.toFloat()),
                ).toInt()

        Timber.d("visibleWordCount: $visibleWordCount")
        val visibleCharacters = wordArray.subList(0, visibleWordCount).joinToString(" ").length

        val spannableString = SpannableString(text)

        if (highlightColor != null && visibleCharacters > 0) {
            spannableString.setSpan(
                BackgroundColorSpan(highlightColor.toColorInt()),
                0,
                visibleCharacters,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }

        val unwrittenColor =
            Color.argb(
                (unwrittenTextAlpha * 255).toInt(),
                Color.red(textView.currentTextColor),
                Color.green(textView.currentTextColor),
                Color.blue(textView.currentTextColor),
            )
        spannableString.setSpan(
            ForegroundColorSpan(unwrittenColor),
            maxOf(0, visibleCharacters),
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        textView.text = spannableString
        textView.invalidate()

        return this
    }
}
