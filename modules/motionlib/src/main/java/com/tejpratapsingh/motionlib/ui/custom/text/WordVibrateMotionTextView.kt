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
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView
import kotlin.math.sin

/**
 * A TextView that vibrates each word independently with a specified amplitude and frequency.
 */
class WordVibrateMotionTextView(
    context: Context,
    text: String,
    startFrame: Int = 0,
    endFrame: Int = -1,
    val amplitude: Float = 5f,
    val frequency: Float = 0.5f,
    val phaseShiftPerWord: Float = 1.0f,
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
    fontAsset = fontAsset,
    textSizeVariant = textSizeVariant,
    textColor = textColor,
    highlightColor = highlightColor,
    effects = effects,
) {
    private val wordArray = text.split(" ")

    init {
        textView.gravity = Gravity.CENTER
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)

        if (frame !in startFrame..if (endFrame == -1) Int.MAX_VALUE else endFrame) {
            return this
        }

        val spannableString = SpannableString(text)

        var currentIdx = 0
        wordArray.forEachIndexed { index, word ->
            // Different phase for each word to make them vibrate independently
            val phase = index * phaseShiftPerWord
            val offsetX = sin(frame.toDouble() * frequency + phase).toFloat() * amplitude
            val offsetY = sin(frame.toDouble() * frequency + phase + 0.5).toFloat() * (amplitude / 2f)

            val span = WordVibrateSpan(offsetX, offsetY)

            val start = currentIdx
            val end = currentIdx + word.length

            if ((start < end) && (end <= text.length)) {
                spannableString.setSpan(
                    span,
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }

            currentIdx += word.length + 1 // +1 for the space
        }

        textView.text = spannableString
        textView.invalidate()

        return this
    }

    private class WordVibrateSpan(
        private val offsetX: Float,
        private val offsetY: Float,
    ) : ReplacementSpan() {
        override fun getSize(
            paint: Paint,
            text: CharSequence?,
            start: Int,
            end: Int,
            fm: Paint.FontMetricsInt?,
        ): Int = paint.measureText(text, start, end).toInt()

        override fun draw(
            canvas: Canvas,
            text: CharSequence?,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint,
        ) {
            canvas.withTranslation(x = offsetX, y = offsetY) {
                drawText(text!!, start, end, x, y.toFloat(), paint)
            }
        }
    }
}
