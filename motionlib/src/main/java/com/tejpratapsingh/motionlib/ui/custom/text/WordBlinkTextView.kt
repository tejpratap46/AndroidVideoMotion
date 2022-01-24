package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import android.os.Build
import android.util.Log
import android.util.TypedValue
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView
import com.tejpratapsingh.motionlib.utils.Easings
import com.tejpratapsingh.motionlib.utils.Interpolators
import com.tejpratapsingh.motionlib.utils.MotionInterpolator

class WordBlinkTextView(
    context: Context,
    text: String,
    startFrame: Int = 0,
    endFrame: Int = -1
) : AbstractMotionTextView(context, text, startFrame, endFrame) {
    private val TAG by lazy {
        "WordBlinkTextView"
    }

    init {
        textView.maxLines = 1
    }

    private val wordArray = text.split(" ")
    private val wordCount: Int = wordArray.size

    override fun forFrame(frame: Int) {
        super.forFrame(frame)

        val visibleWordCount: Int = MotionInterpolator.interpolateForRange(
            Interpolators(Easings.LINEAR),
            frame,
            Pair(startFrame, endFrame),
            Pair(0f, wordCount.toFloat())
        ).toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textView.setAutoSizeTextTypeUniformWithConfiguration(
                12,
                100,
                1,
                TypedValue.COMPLEX_UNIT_SP
            )
        }

        Log.d(TAG, "visibleWordCount: $visibleWordCount")

        textView.text = wordArray[maxOf(visibleWordCount - 1, 0)]
    }
}