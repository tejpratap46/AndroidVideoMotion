package com.tejpratapsingh.motionlib.ui.effects

import android.graphics.ColorMatrix
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
 * A [MotionEffect] that applies a grayscale filter using [RenderEffect].
 * Animates saturation from [fromSaturation] to [toSaturation].
 * Saturation 1.0 is full color, 0.0 is grayscale.
 */
class GrayscaleEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val fromSaturation: Float = 1.0f,
    val toSaturation: Float = 0.0f,
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView
        val view = motionView as View

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return motionView

        if (frame !in startFrame..endFrame) {
            if (frame > endFrame) {
                // Keep the final state if needed, or clear it. 
                // Typically transitions might want to stay at final state if it's the end of visibility.
                // But for generic effects, we might want to clear them when out of range.
                // BlurEffect clears it, so we follow that pattern.
                view.setRenderEffect(null)
            }
            return motionView
        }

        val saturation = MotionInterpolator.interpolateForRange(
            interpolator = Interpolators(Easings.LINEAR),
            currentFrame = frame,
            frameRange = Pair(startFrame, endFrame),
            valueRange = Pair(fromSaturation, toSaturation),
        )

        val matrix = ColorMatrix().apply {
            setSaturation(saturation)
        }
        
        val colorFilter = ColorMatrixColorFilter(matrix)
        view.setRenderEffect(RenderEffect.createColorFilterEffect(colorFilter))

        return motionView
    }
}
