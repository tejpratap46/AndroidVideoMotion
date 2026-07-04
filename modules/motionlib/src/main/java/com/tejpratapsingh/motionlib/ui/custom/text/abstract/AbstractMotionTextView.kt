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

/**
 * An abstract base class for motion-based text views.
 * Extends [BaseContourMotionView] to provide common functionality for rendering text with motion effects,
 * custom fonts, and styling within a contour layout.
 *
 * @param context The Android context.
 * @param text The text to be displayed.
 * @param startFrame The frame index where the text starts appearing.
 * @param endFrame The frame index where the text stops appearing. If -1, it stays indefinitely or until the end of the video.
 * @param textView The underlying [AppCompatTextView] used for rendering.
 * @param writingSpeed The speed factor for text "writing" animations. Defaults to 1.0f.
 * @param fontUrl Optional URL for a custom web font.
 * @param textSizeVariant Optional variant for text size (e.g., TITLE, BODY).
 * @param textColor Optional hex color string for the text.
 * @param highlightColor Optional hex color string for highlighting parts of the text.
 * @param effects A list of [MotionEffect]s to be applied to this view.
 */
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
    /**
     * Calculates the adjusted end frame based on the writing speed.
     * If [writingSpeed] is 1.0, it matches [endFrame]. If higher, the duration is shortened.
     */
    protected val inferredEndFrame: Int =
        if (endFrame != -1 && writingSpeed > 0) {
            (startFrame + (endFrame - startFrame) / writingSpeed).toInt()
        } else {
            endFrame
        }

    init {
        // Set the contour dimensions based on the current configuration's aspect ratio.
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

        // Center the text within the TextView.
        textView.gravity = Gravity.CENTER

        // Apply text styling and custom fonts.
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
                    // Fallback or log error if the color string is invalid
                }
            }
            if (fontUrl != null) {
                typeface = getWebFont(fontUrl)
            }
        }

        // Position the TextView to fill the parent container using Contour.
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
        // Set the final text content.
        textView.text = text
    }
}
