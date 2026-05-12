package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionlib.ui.custom.CutoutTextView
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView
import timber.log.Timber

class WordBlinkTextView(
    context: Context,
    text: String,
    startFrame: Int = 0,
    endFrame: Int = -1,
    writingSpeed: Float = 0f,
    textView: AppCompatTextView = AppCompatTextView(context),
    effects: List<MotionEffect> = emptyList(),
) : AbstractMotionTextView(
        context = context,
        text = text,
        startFrame = startFrame,
        endFrame = endFrame,
        textView = textView,
        writingSpeed = writingSpeed,
        effects = effects,
    ) {
    init {
        textView.maxLines = 1

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

    private val wordArray = text.split(" ")
    private val wordCount: Int = wordArray.size

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)

        val visibleWordCount: Int =
            MotionInterpolator
                .interpolateForRange(
                    Interpolators(Easings.LINEAR),
                    frame,
                    Pair(startFrame, endFrame),
                    Pair(0f, wordCount.toFloat()),
                ).toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (textView as TextView).setAutoSizeTextTypeUniformWithConfiguration(
                12,
                100,
                1,
                TypedValue.COMPLEX_UNIT_SP,
            )
        }

        Timber.d("visibleWordCount: $visibleWordCount")

        textView.text = wordArray[maxOf(visibleWordCount - 1, 0)]
        textView.invalidate()

        return this
    }
}
