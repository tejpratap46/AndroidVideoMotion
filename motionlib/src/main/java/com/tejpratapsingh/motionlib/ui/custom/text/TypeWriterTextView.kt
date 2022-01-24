package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView
import com.tejpratapsingh.motionlib.utils.Easings
import com.tejpratapsingh.motionlib.utils.Interpolators
import com.tejpratapsingh.motionlib.utils.MotionInterpolator

class TypeWriterTextView(
    context: Context,
    private val text: String,
    startFrame: Int,
    endFrame: Int
) :
    AbstractMotionTextView(context, text, startFrame, endFrame) {
    private val TAG by lazy {
        "TypeWriterTextView"
    }

    override fun forFrame(frame: Int) {
        super.forFrame(frame)

        val visibleCharsCount: Int = MotionInterpolator.interpolateForRange(
            Interpolators(Easings.LINEAR),
            frame,
            Pair(startFrame, endFrame),
            Pair(0f, text.length.toFloat())
        ).toInt()

        Log.d(TAG, "visibleCharsCount: $visibleCharsCount")

        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(Color.TRANSPARENT),
            maxOf(0, visibleCharsCount),
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.text = spannableString
    }
}