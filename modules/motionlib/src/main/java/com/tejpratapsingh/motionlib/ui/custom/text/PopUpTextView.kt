package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ReplacementSpan
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withTranslation
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionlib.ui.custom.CutoutTextView
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView
import timber.log.Timber

/**
 * A TextView that animates each word popping up from the bottom.
 * Similar to WordWriterTextView but with vertical translation and alpha animation per word.
 */
class PopUpTextView(
    context: Context,
    private val text: String,
    startFrame: Int = 0,
    endFrame: Int = -1,
    writingSpeed: Float = 0f,
    private val unwrittenTextAlpha: Float = 0f,
    private val maxTranslationY: Float = 50f,
    textView: AppCompatTextView = CutoutTextView(context),
) : AbstractMotionTextView(
    context = context,
    text = text,
    startFrame = startFrame,
    endFrame = endFrame,
    textView = textView,
    writingSpeed = writingSpeed,
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

        val progress: Float =
            MotionInterpolator
                .interpolateForRange(
                    Interpolators(Easings.LINEAR),
                    frame,
                    Pair(startFrame, inferredEndFrame),
                    Pair(0f, wordCount.toFloat()),
                )

        val spannableString = SpannableString(text)
        
        var currentIdx = 0
        wordArray.forEachIndexed { index, word ->
            val wordProgress = (progress - index).coerceIn(0f, 1f)
            
            val span = PopUpSpan(
                progress = wordProgress,
                unwrittenAlpha = unwrittenTextAlpha,
                maxTranslationY = maxTranslationY,
            )
            
            val start = currentIdx
            val end = currentIdx + word.length
            
            if ((start < end) && (end <= text.length)) {
                spannableString.setSpan(
                    span,
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            
            currentIdx += word.length + 1 // +1 for the space
        }

        textView.text = spannableString
        textView.invalidate()

        return this
    }

    /**
     * Internal span to handle individual word animations.
     */
    private class PopUpSpan(
        private val progress: Float,
        private val unwrittenAlpha: Float,
        private val maxTranslationY: Float,
    ) : ReplacementSpan() {

        override fun getSize(
            paint: Paint,
            text: CharSequence?,
            start: Int,
            end: Int,
            fm: Paint.FontMetricsInt?
        ): Int {
            return paint.measureText(text, start, end).toInt()
        }

        override fun draw(
            canvas: Canvas,
            text: CharSequence?,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint
        ) {
            if (progress <= 0f) {
                return
            }

            val alpha = (unwrittenAlpha + (1f - unwrittenAlpha) * progress) * 255
            val translationY = maxTranslationY * (1f - progress)
            
            val oldAlpha = paint.alpha
            paint.alpha = alpha.toInt()
            
            canvas.withTranslation(y = translationY) {
                drawText(text!!, start, end, x, y.toFloat(), paint)
            }
            
            paint.alpha = oldAlpha
        }
    }
}
