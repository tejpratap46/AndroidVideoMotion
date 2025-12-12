package com.tejpratapsingh.motionlib.tensorflow

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.core.graphics.set

object ImageUtils {
    fun bitmapToFloatBuffer(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val width = bitmap.width
        val height = bitmap.height
        val buffer = Array(1) { Array(height) { Array(width) { FloatArray(3) } } }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val px = bitmap.getPixel(x, y)
                buffer[0][y][x][0] = Color.red(px) / 255.0f
                buffer[0][y][x][1] = Color.green(px) / 255.0f
                buffer[0][y][x][2] = Color.blue(px) / 255.0f
            }
        }
        return buffer
    }

    fun floatArrayToBitmap(output: Array<Array<FloatArray>>): Bitmap {
        val height = output.size
        val width = output[0].size
        val bitmap = createBitmap(width, height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val r = (output[y][x][0] * 255).toInt().coerceIn(0, 255)
                val g = (output[y][x][1] * 255).toInt().coerceIn(0, 255)
                val b = (output[y][x][2] * 255).toInt().coerceIn(0, 255)
                bitmap[x, y] = Color.rgb(r, g, b)
            }
        }
        return bitmap
    }

    fun applyMask(
        original: Bitmap,
        mask: Array<FloatArray>,
    ): Bitmap {
        val width = original.width
        val height = original.height
        val scaledMask = floatArrayToGrayscaleBitmap(mask).scale(width, height)

        val output = createBitmap(width, height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val alpha = Color.red(scaledMask[x, y])
                val pixel = original[x, y]
                output[x, y] =
                    Color.argb(alpha, Color.red(pixel), Color.green(pixel), Color.blue(pixel))
            }
        }
        return output
    }

    fun floatArrayToGrayscaleBitmap(mask: Array<FloatArray>): Bitmap {
        val height = mask.size
        val width = mask[0].size
        val bitmap = createBitmap(width, height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val v = (mask[y][x] * 255).toInt().coerceIn(0, 255)
                bitmap[x, y] = Color.rgb(v, v, v)
            }
        }
        return bitmap
    }

    fun intArrayToGrayscaleBitmap(mask: Array<IntArray>): Bitmap {
        val height = mask.size
        val width = mask[0].size
        val bitmap = createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val value = mask[y][x].coerceIn(0, 255)
                pixels[y * width + x] = Color.argb(value, 0, 0, 0)
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}
