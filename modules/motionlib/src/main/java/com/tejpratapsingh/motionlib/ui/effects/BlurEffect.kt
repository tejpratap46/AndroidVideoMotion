package com.tejpratapsingh.motionlib.ui.effects

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator

class BlurEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val fromBlurRadius: Float = 0.1f,
    val toBlurRadius: Float = 20f,
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView

        val view = motionView as View

        if (frame !in startFrame..endFrame) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                view.setRenderEffect(null)
            }
            return motionView
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blurRadius =
                MotionInterpolator.interpolateForRange(
                    interpolator = Interpolators(Easings.LINEAR),
                    currentFrame = frame,
                    frameRange = Pair(startFrame, endFrame),
                    valueRange = Pair(fromBlurRadius, toBlurRadius),
                )

            view.setRenderEffect(
                RenderEffect.createBlurEffect(
                    blurRadius,
                    blurRadius,
                    Shader.TileMode.CLAMP,
                ),
            )
        }

        return motionView
    }
}
