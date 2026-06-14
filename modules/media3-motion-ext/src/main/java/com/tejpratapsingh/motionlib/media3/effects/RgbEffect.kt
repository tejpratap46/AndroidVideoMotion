package com.tejpratapsingh.motionlib.media3.effects

import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.os.Build
import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.RgbAdjustment
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.media3.Media3Utils

/**
 * A [MotionEffect] that adjusts RGB scaling using [androidx.media3.effect.RgbAdjustment].
 */
@OptIn(UnstableApi::class)
class RgbEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val fromRed: Float = 1f,
    val toRed: Float = 1f,
    val fromGreen: Float = 1f,
    val toGreen: Float = 1f,
    val fromBlue: Float = 1f,
    val toBlue: Float = 1f,
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

        val r = MotionInterpolator.interpolateForRange(
            interpolator = Interpolators(Easings.LINEAR),
            currentFrame = frame,
            frameRange = Pair(startFrame, endFrame),
            valueRange = Pair(fromRed, toRed),
        )

        val g = MotionInterpolator.interpolateForRange(
            interpolator = Interpolators(Easings.LINEAR),
            currentFrame = frame,
            frameRange = Pair(startFrame, endFrame),
            valueRange = Pair(fromGreen, toGreen),
        )

        val b = MotionInterpolator.interpolateForRange(
            interpolator = Interpolators(Easings.LINEAR),
            currentFrame = frame,
            frameRange = Pair(startFrame, endFrame),
            valueRange = Pair(fromBlue, toBlue),
        )

        val rgbAdjustment = RgbAdjustment.Builder()
            .setRedScale(r)
            .setGreenScale(g)
            .setBlueScale(b)
            .build()
            
        val matrix = rgbAdjustment.getMatrix(0L, false)
        val colorMatrix = Media3Utils.toColorMatrix(matrix)
        
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        view.setRenderEffect(RenderEffect.createColorFilterEffect(colorFilter))

        return motionView
    }
}
