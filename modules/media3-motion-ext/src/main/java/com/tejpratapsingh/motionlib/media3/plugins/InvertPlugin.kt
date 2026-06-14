package com.tejpratapsingh.motionlib.media3.plugins

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.RgbFilter
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.media3.Media3Utils

/**
 * A [MotionPlugin] that applies an inverted color filter using [androidx.media3.effect.RgbFilter].
 */
@OptIn(UnstableApi::class)
class InvertPlugin : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(input.width, input.height, input.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()

        val invertedFilter = RgbFilter.createInvertedFilter()
        val matrix = invertedFilter.getMatrix(0L, false)
        val colorMatrix = Media3Utils.toColorMatrix(matrix)

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(input, 0f, 0f, paint)

        return output
    }
}
