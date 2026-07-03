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
 * A [MotionEffect] that inverts the colors of the view using [RenderEffect].
 * Animates intensity from [fromIntensity] to [toIntensity].
 */
class InvertEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val fromIntensity: Float = 0.0f,
    val toIntensity: Float = 1.0f,
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

        val intensity =
            MotionInterpolator.interpolateForRange(
                interpolator = Interpolators(Easings.LINEAR),
                currentFrame = frame,
                frameRange = Pair(startFrame, endFrame),
                valueRange = Pair(fromIntensity, toIntensity),
            )

        // Invert matrix:
        // R' = -1*R + 255
        // G' = -1*G + 255
        // B' = -1*B + 255
        // Scaled to 0-1 for ColorMatrix:
        // R' = -1*R + 1

        val invertMatrix =
            floatArrayOf(
                -1f,
                0f,
                0f,
                0f,
                255f,
                0f,
                -1f,
                0f,
                0f,
                255f,
                0f,
                0f,
                -1f,
                0f,
                255f,
                0f,
                0f,
                0f,
                1f,
                0f,
            )

        val identityMatrix =
            floatArrayOf(
                1f,
                0f,
                0f,
                0f,
                0f,
                0f,
                1f,
                0f,
                0f,
                0f,
                0f,
                0f,
                1f,
                0f,
                0f,
                0f,
                0f,
                0f,
                1f,
                0f,
            )

        val resultMatrix = FloatArray(20)
        for (i in 0 until 20) {
            resultMatrix[i] = identityMatrix[i] + (invertMatrix[i] - identityMatrix[i]) * intensity
        }

        val colorFilter = ColorMatrixColorFilter(resultMatrix)
        view.setRenderEffect(RenderEffect.createColorFilterEffect(colorFilter))

        return motionView
    }
}
