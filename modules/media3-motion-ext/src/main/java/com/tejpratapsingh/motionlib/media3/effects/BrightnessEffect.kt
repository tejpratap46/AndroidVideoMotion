package com.tejpratapsingh.motionlib.media3.effects

import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.os.Build
import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Brightness
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.media3.Media3Utils

/**
 * A [MotionEffect] that adjusts brightness using [androidx.media3.effect.Brightness].
 * Animates brightness from [fromBrightness] to [toBrightness].
 * Brightness ranges from -1 (black) to 1 (white). 0 is no change.
 */
@OptIn(UnstableApi::class)
class BrightnessEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val fromBrightness: Float = 0.0f,
    val toBrightness: Float = 1.0f,
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

        val brightnessValue = MotionInterpolator.interpolateForRange(
            interpolator = Interpolators(Easings.LINEAR),
            currentFrame = frame,
            frameRange = Pair(startFrame, endFrame),
            valueRange = Pair(fromBrightness, toBrightness),
        )

        val brightnessEffect = Brightness(brightnessValue)
        val matrix = brightnessEffect.getMatrix(0L, false)
        val colorMatrix = Media3Utils.toColorMatrix(matrix)
        
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        view.setRenderEffect(RenderEffect.createColorFilterEffect(colorFilter))

        return motionView
    }
}
