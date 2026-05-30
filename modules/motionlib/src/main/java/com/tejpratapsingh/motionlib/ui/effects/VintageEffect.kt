package com.tejpratapsingh.motionlib.ui.effects

import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator

/**
 * A [MotionEffect] that applies a vintage filter by combining Sepia and Vignette.
 * Animates intensity from [fromIntensity] to [toIntensity].
 */
class VintageEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val fromIntensity: Float = 0.0f,
    val toIntensity: Float = 1.0f,
) : MotionEffect {
    override lateinit var motionView: MotionView

    @Suppress("ktlint:standard:property-naming")
    private val VIGNETTE_SHADER =
        """
        uniform shader content;
        uniform float2 size;
        uniform float intensity;

        half4 main(float2 fragCoord) {
            half4 color = content.eval(fragCoord);
            float2 uv = fragCoord / size;
            uv *=  1.0 - uv.yx;
            float vig = uv.x*uv.y * 15.0;
            vig = pow(vig, intensity);
            return color * vig;
        }
        """.trimIndent()

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

        // Sepia matrix (standard)
        val sepiaMatrix =
            floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            )

        // We can interpolate between identity and sepia
        val identityMatrix =
            floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            )

        val resultMatrix = FloatArray(20)
        for (i in 0 until 20) {
            resultMatrix[i] = identityMatrix[i] + (sepiaMatrix[i] - identityMatrix[i]) * intensity
        }

        val sepiaEffect = RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(resultMatrix))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shader = RuntimeShader(VIGNETTE_SHADER)
            shader.setFloatUniform("size", view.width.toFloat(), view.height.toFloat())
            shader.setFloatUniform("intensity", intensity * 0.5f) // Scale vignette intensity

            val vignetteEffect = RenderEffect.createRuntimeShaderEffect(shader, "content")
            view.setRenderEffect(RenderEffect.createChainEffect(vignetteEffect, sepiaEffect))
        } else {
            view.setRenderEffect(sepiaEffect)
        }

        return motionView
    }
}
