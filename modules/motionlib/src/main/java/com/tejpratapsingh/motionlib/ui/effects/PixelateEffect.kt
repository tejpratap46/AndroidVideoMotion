package com.tejpratapsingh.motionlib.ui.effects

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
 * A [MotionEffect] that applies a pixelation effect using AGSL.
 * Animates pixel size from [fromPixelSize] to [toPixelSize].
 */
class PixelateEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val fromPixelSize: Float = 1f,
    val toPixelSize: Float = 20f,
) : MotionEffect {
    override lateinit var motionView: MotionView

    companion object {
        private const val PIXELATE_SHADER =
            """
        uniform shader content;
        uniform float pixelSize;

        half4 main(float2 fragCoord) {
            if (pixelSize <= 1.0) {
                return content.eval(fragCoord);
            }
            float2 p = floor(fragCoord / pixelSize) * pixelSize;
            return content.eval(p);
        }
    """
    }

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView
        val view = motionView as View

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return motionView

        if (frame !in startFrame..endFrame) {
            if (frame > endFrame) {
                view.setRenderEffect(null)
            }
            return motionView
        }

        val pixelSize =
            MotionInterpolator.interpolateForRange(
                interpolator = Interpolators(Easings.LINEAR),
                currentFrame = frame,
                frameRange = Pair(startFrame, endFrame),
                valueRange = Pair(fromPixelSize, toPixelSize),
            )

        val shader = RuntimeShader(PIXELATE_SHADER)
        shader.setFloatUniform("pixelSize", pixelSize)

        view.setRenderEffect(RenderEffect.createRuntimeShaderEffect(shader, "content"))

        return motionView
    }
}
