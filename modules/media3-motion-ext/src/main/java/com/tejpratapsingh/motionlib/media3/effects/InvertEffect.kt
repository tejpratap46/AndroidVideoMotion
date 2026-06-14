package com.tejpratapsingh.motionlib.media3.effects

import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.os.Build
import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.RgbFilter
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.media3.Media3Utils

/**
 * A [MotionEffect] that applies an inverted color filter using [androidx.media3.effect.RgbFilter].
 */
@OptIn(UnstableApi::class)
class InvertEffect(
    override val startFrame: Int,
    override val endFrame: Int,
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

        val invertedFilter = RgbFilter.createInvertedFilter()
        val matrix = invertedFilter.getMatrix(0L, false)
        val colorMatrix = Media3Utils.toColorMatrix(matrix)
        
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        view.setRenderEffect(RenderEffect.createColorFilterEffect(colorFilter))

        return motionView
    }
}
