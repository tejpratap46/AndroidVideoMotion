package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
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

class TypeWriterTextView(
    context: Context,
    text: String,
    startFrame: Int,
    endFrame: Int,
    writingSpeed: Float = 0f,
    val unwrittenTextAlpha: Float = 0f,
    val cursorChar: String? = "|",
    val blinkFrameRate: Int = 0,
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

        val visibleCharsCount: Int =
            MotionInterpolator
                .interpolateForRange(
                    Interpolators(Easings.LINEAR),
                    frame,
                    Pair(startFrame, endFrame),
                    Pair(0f, text.length.toFloat()),
                ).toInt()

        Timber.d("visibleCharsCount: $visibleCharsCount")

        // I can change the logic to something like this:
        val showCursor = cursorChar != null && (blinkFrameRate <= 0 || frame / blinkFrameRate % 2 == 0)
        val displayText = StringBuilder()
        displayText.append(text.substring(0, visibleCharsCount))
        if (showCursor) {
            displayText.append(cursorChar)
        }
        displayText.append(text.substring(visibleCharsCount))

        val spannableString = SpannableString(displayText.toString())
        val unwrittenColor =
            Color.argb(
                (unwrittenTextAlpha * 255).toInt(),
                Color.red(textView.currentTextColor),
                Color.green(textView.currentTextColor),
                Color.blue(textView.currentTextColor),
            )

        val unwrittenStart = visibleCharsCount + if (showCursor) cursorChar.length else 0

        if (unwrittenStart < spannableString.length) {
            spannableString.setSpan(
                ForegroundColorSpan(unwrittenColor),
                unwrittenStart,
                spannableString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }

        textView.text = spannableString

        textView.invalidate()
        return this
    }
}
