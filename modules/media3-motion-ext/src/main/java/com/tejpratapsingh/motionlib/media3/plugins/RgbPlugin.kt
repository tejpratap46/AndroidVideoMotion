package com.tejpratapsingh.motionlib.media3.plugins

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.RgbAdjustment
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.media3.Media3Utils

/**
 * A [MotionPlugin] that adjusts RGB scaling using [androidx.media3.effect.RgbAdjustment].
 */
@OptIn(UnstableApi::class)
class RgbPlugin(
    val redScale: Float = 1f,
    val greenScale: Float = 1f,
    val blueScale: Float = 1f,
) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(input.width, input.height, input.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()

        val rgbAdjustment = RgbAdjustment.Builder()
            .setRedScale(redScale)
            .setGreenScale(greenScale)
            .setBlueScale(blueScale)
            .build()
            
        val matrix = rgbAdjustment.getMatrix(0L, false)
        val colorMatrix = Media3Utils.toColorMatrix(matrix)

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(input, 0f, 0f, paint)

        return output
    }
}
