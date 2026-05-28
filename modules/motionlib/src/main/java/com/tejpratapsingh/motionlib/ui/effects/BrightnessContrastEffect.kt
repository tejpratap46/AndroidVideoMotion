package com.tejpratapsingh.motionlib.ui.effects

import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.os.Build
import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator

/**
 * A [MotionEffect] that adjusts brightness and contrast using [RenderEffect].
 * Brightness: 0.0 is identity, negative is darker, positive is brighter.
 * Contrast: 1.0 is identity, higher is more contrast.
 */
class BrightnessContrastEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val fromBrightness: Float = 0.0f,
    val toBrightness: Float = 0.0f,
    val fromContrast: Float = 1.0f,
    val toContrast: Float = 1.0f,
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView
        val view = motionView as View

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return motionView

        if (frame !in startFrame..endFrame) {
            if (frame > endFrame) {
                view.setRenderEffect(null)
            }
            return motionView
        }

        val brightness = MotionInterpolator.interpolateForRange(
            interpolator = Interpolators(Easings.LINEAR),
            currentFrame = frame,
            frameRange = Pair(startFrame, endFrame),
            valueRange = Pair(fromBrightness, toBrightness),
        )
        
        val contrast = MotionInterpolator.interpolateForRange(
            interpolator = Interpolators(Easings.LINEAR),
            currentFrame = frame,
            frameRange = Pair(startFrame, endFrame),
            valueRange = Pair(fromContrast, toContrast),
        )

        // Matrix for contrast and brightness
        // contrast * (channel - 0.5) + 0.5 + brightness
        // = contrast * channel - 0.5 * contrast + 0.5 + brightness
        val t = (1.0f - contrast) / 2.0f * 255.0f + brightness * 255.0f
        
        val matrix = floatArrayOf(
            contrast, 0f, 0f, 0f, t,
            0f, contrast, 0f, 0f, t,
            0f, 0f, contrast, 0f, t,
            0f, 0f, 0f, 1f, 0f
        )

        val colorFilter = ColorMatrixColorFilter(matrix)
        view.setRenderEffect(RenderEffect.createColorFilterEffect(colorFilter))

        return motionView
    }
}
