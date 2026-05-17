package com.tejpratapsingh.motionlib.ui.custom.text.abstract

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionTextSizeProvider
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionlib.utils.getWebFont

abstract class AbstractMotionTextView(
    context: Context,
    val text: String,
    startFrame: Int,
    endFrame: Int,
    val textView: AppCompatTextView,
    val writingSpeed: Float = 1f,
    val fontUrl: String? = null,
    val textSizeVariant: MotionTextVariant? = null,
    val textColor: String? = null,
    val highlightColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
) : BaseContourMotionView(context, startFrame, endFrame, effects = effects) {
    protected val inferredEndFrame: Int =
        if (endFrame != -1 && writingSpeed > 0) {
            (startFrame + (endFrame - startFrame) / writingSpeed).toInt()
        } else {
            endFrame
        }

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

        textView.apply {
            textSizeVariant?.let { variant ->
                val config = provideCurrentConfig()
                val fontSize = MotionTextSizeProvider.getFontSize(config.aspectRatio, variant)
                this.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
            }
            textColor?.let {
                try {
                    this.setTextColor(it.toColorInt())
                } catch (e: Exception) {
                    // Fallback or log error
                }
            }
            if (fontUrl != null) {
                typeface = getWebFont(fontUrl)
            }
        }

        textView.layoutBy(
            x =
                leftTo {
                    parent.left()
                }.rightTo {
                    parent.right()
                },
            y =
                topTo {
                    parent.top()
                }.bottomTo {
                    parent.bottom()
                },
        )
        textView.text = text
    }
}
