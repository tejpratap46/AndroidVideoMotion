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
 * A [MotionEffect] that applies a sepia filter using [RenderEffect].
 * Animates intensity from [fromIntensity] to [toIntensity].
 */
class SepiaEffect(
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

        val intensity = MotionInterpolator.interpolateForRange(
            interpolator = Interpolators(Easings.LINEAR),
            currentFrame = frame,
            frameRange = Pair(startFrame, endFrame),
            valueRange = Pair(fromIntensity, toIntensity),
        )

        // Sepia matrix (standard)
        // R' = (R * .393) + (G * .769) + (B * .189)
        // G' = (R * .349) + (G * .686) + (B * .168)
        // B' = (R * .272) + (G * .534) + (B * .131)
        
        val sepiaMatrix = floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        
        // We can interpolate between identity and sepia
        val identityMatrix = floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        
        val resultMatrix = FloatArray(20)
        for (i in 0 until 20) {
            resultMatrix[i] = identityMatrix[i] + (sepiaMatrix[i] - identityMatrix[i]) * intensity
        }

        val colorFilter = ColorMatrixColorFilter(resultMatrix)
        view.setRenderEffect(RenderEffect.createColorFilterEffect(colorFilter))

        return motionView
    }
}
