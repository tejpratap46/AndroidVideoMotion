package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView

class WordWriterTextView(
    context: Context,
    private val text: String,
    startFrame: Int = 0,
    endFrame: Int = -1
) : AbstractMotionTextView(context, text, startFrame, endFrame) {
    private val TAG by lazy {
        "WordWriterTextView"
    }

    private val wordArray = text.split(" ")
    private val wordCount: Int = wordArray.size

    override fun forFrame(frame: Int): View {
        super.forFrame(frame)

        val visibleWordCount: Int = MotionInterpolator.interpolateForRange(
            Interpolators(Easings.LINEAR),
            frame,
            Pair(startFrame, endFrame),
            Pair(0f, wordCount.toFloat())
        ).toInt()

        Log.d(TAG, "visibleWordCount: $visibleWordCount")
        val visibleCharacters = wordArray.subList(0, visibleWordCount).joinToString(" ").length

        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(Color.TRANSPARENT),
            maxOf(0, visibleCharacters),
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.text = spannableString

        return this
    }
}