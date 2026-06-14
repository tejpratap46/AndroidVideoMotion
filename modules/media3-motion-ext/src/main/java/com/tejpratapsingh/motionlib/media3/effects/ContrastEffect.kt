package com.tejpratapsingh.motionlib.media3.effects

import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.os.Build
import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Contrast
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.media3.Media3Utils

/**
 * A [MotionEffect] that adjusts contrast using [androidx.media3.effect.Contrast].
 * Animates contrast from [fromContrast] to [toContrast].
 * Contrast 1.0 is no change. 0.0 is uniform gray. > 1.0 increases contrast.
 */
@OptIn(UnstableApi::class)
class ContrastEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val fromContrast: Float = 1.0f,
    val toContrast: Float = 2.0f,
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

        val contrastValue = MotionInterpolator.interpolateForRange(
            interpolator = Interpolators(Easings.LINEAR),
            currentFrame = frame,
            frameRange = Pair(startFrame, endFrame),
            valueRange = Pair(fromContrast, toContrast),
        )

        val contrastEffect = Contrast(contrastValue)
        val matrix = contrastEffect.getMatrix(0L, false)
        val colorMatrix = Media3Utils.toColorMatrix(matrix)
        
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        view.setRenderEffect(RenderEffect.createColorFilterEffect(colorFilter))

        return motionView
    }
}
