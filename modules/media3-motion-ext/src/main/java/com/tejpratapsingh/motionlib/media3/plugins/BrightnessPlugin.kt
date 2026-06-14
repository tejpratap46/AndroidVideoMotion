package com.tejpratapsingh.motionlib.media3.plugins

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Brightness
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.media3.Media3Utils

/**
 * A [MotionPlugin] that adjusts brightness using [androidx.media3.effect.Brightness].
 * Brightness ranges from -1 (black) to 1 (white). 0 is no change.
 */
@OptIn(UnstableApi::class)
class BrightnessPlugin(val brightness: Float) : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(input.width, input.height, input.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()

        val brightnessEffect = Brightness(brightness)
        val matrix = brightnessEffect.getMatrix(0L, false)
        val colorMatrix = Media3Utils.toColorMatrix(matrix)

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(input, 0f, 0f, paint)

        return output
    }
}
