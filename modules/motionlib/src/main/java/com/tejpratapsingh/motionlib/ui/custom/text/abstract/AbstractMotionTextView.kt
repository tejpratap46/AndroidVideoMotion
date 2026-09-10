package com.tejpratapsingh.motionlib.ui.custom.text.abstract

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.getFontSize
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
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
 * @param fontAsset Optional [MotionAsset] for a custom web font.
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
    val fontAsset: MotionAsset? = null,
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
        // Center the text within the TextView.
        textView.gravity = Gravity.CENTER

        // Apply text styling and custom fonts.
        textView.apply {
            textColor?.let {
                try {
                    this.setTextColor(it.toColorInt())
                } catch (e: Exception) {
                    // Fallback or log error if the color string is invalid
                }
            }
            fontAsset?.let {
                typeface = getWebFont(it.getUri().toString())
            }
        }

        // Ensure internal coordinate system matches layoutInfo if set (e.g. when used in stacks)
        contourWidthOf {
            if (layoutInfo.width > 0) {
                layoutInfo.width.toXInt()
            } else {
                motionConfig.aspectRatio.width.toXInt()
            }
        }
        contourHeightOf {
            if (layoutInfo.height > 0) {
                layoutInfo.height.toYInt()
            } else {
                motionConfig.aspectRatio.height.toYInt()
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

    private var isTextSizeInitialized = false

    override fun forFrame(frame: Int): MotionView {
        if (!isTextSizeInitialized) {
            textSizeVariant?.let { variant ->
                val fontSize = motionConfig.getFontSize(variant)
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
            }
            isTextSizeInitialized = true
        }
        return super.forFrame(frame)
    }
}
