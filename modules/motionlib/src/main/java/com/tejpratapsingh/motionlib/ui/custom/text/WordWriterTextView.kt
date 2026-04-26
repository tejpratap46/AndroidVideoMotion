package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionlib.ui.custom.CutoutTextView
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView
import timber.log.Timber

class WordWriterTextView(
    context: Context,
    private val text: String,
    startFrame: Int = 0,
    endFrame: Int = -1,
    writingSpeed: Float = 0f,
    private val unwrittenTextAlpha: Float = 0f,
    textView: AppCompatTextView = CutoutTextView(context),
) : AbstractMotionTextView(context, text, startFrame, endFrame, textView, writingSpeed) {
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
