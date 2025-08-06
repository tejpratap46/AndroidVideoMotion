package com.tejpratapsingh.motionlib.tensorflow

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.core.graphics.set
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import kotlin.math.ceil
import kotlin.math.min

class TiledBackgroundRemover(
    context: Context,
    modelPath: String,
    private val tileSize: Int = 257,
    private val overlap: Int = 64
) {
    private val interpreter: Interpreter

    init {
        val model = FileUtil.loadMappedFile(context, modelPath)
        interpreter = Interpreter(model)
    }

    fun removeBackgroundTiled(inputBitmap: Bitmap): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height
        val stride = tileSize - overlap

        // Full-size output mask (grayscale)
        val fullMask = createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val maskCanvas = Canvas(fullMask)
        val paint = Paint().apply { isFilterBitmap = true }

        val tilesX = ceil((width - overlap).toFloat() / stride).toInt()
        val tilesY = ceil((height - overlap).toFloat() / stride).toInt()

        // For blending overlapping tiles: accumulate alpha and count
        val alphaAcc = Array(height) { IntArray(width) { 0 } }
        val alphaCount = Array(height) { IntArray(width) { 0 } }

        for (ty in 0 until tilesY) {
            for (tx in 0 until tilesX) {
                val x0 = tx * stride
                val y0 = ty * stride
                val w = min(tileSize, width - x0)
                val h = min(tileSize, height - y0)

                val tile = Bitmap.createBitmap(inputBitmap, x0, y0, w, h)
                val resized = tile.scale(tileSize, tileSize)

                // Run inference
                val maskTile = runMaskOnTile(resized) // returns grayscale 320×320 bitmap

                // Resize back to tile rectangle size
                val maskResized = maskTile.scale(w, h)

                // Blend the mask into accumulators
                for (dy in 0 until h) {
                    for (dx in 0 until w) {
                        val px = Color.red(maskResized[dx, dy])
                        val globalX = x0 + dx
                        val globalY = y0 + dy
                        alphaAcc[globalY][globalX] += px
                        alphaCount[globalY][globalX] += 1
                    }
                }
            }
        }

        // Build final mask by averaging
        val maskPixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val acc = alphaAcc[y][x]
                val cnt = alphaCount[y][x]
                val avg = if (cnt > 0) acc / cnt else 0
                maskPixels[y * width + x] = Color.argb(avg, 0, 0, 0)
            }
        }
        fullMask.setPixels(maskPixels, 0, width, 0, 0, width, height)

        // Apply mask to original
        return applyMaskToBitmap(inputBitmap, fullMask)
    }

    private fun runMaskOnTile(resizedTile: Bitmap): Bitmap {
        // Convert to float input [1, H, W, 3]
        val inputBuffer = ImageUtils.bitmapToFloatBuffer(resizedTile)
        // Model output as [1, tileSize, tileSize, numClasses]
        val numClasses = 21
        val out = Array(1) { Array(tileSize) { Array(tileSize) { FloatArray(numClasses) } } }
        interpreter.run(inputBuffer, out)

        // Convert to single-channel mask by argmax
        val mask = Array(tileSize) { IntArray(tileSize) }
        for (y in 0 until tileSize) {
            for (x in 0 until tileSize) {
                val classProbs = out[0][y][x]
                mask[y][x] = classProbs.indices.maxByOrNull { classProbs[it] } ?: 0
            }
        }
        // Convert mask to grayscale bitmap (e.g., 255 for foreground, 0 for background)
        return ImageUtils.intArrayToGrayscaleBitmap(mask)
    }

    private fun applyMaskToBitmap(src: Bitmap, mask: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val result = createBitmap(w, h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val alpha = Color.red(mask[x, y])
                val px = src[x, y]
                result[x, y] = Color.argb(alpha, Color.red(px), Color.green(px), Color.blue(px))
            }
        }
        return result
    }
}
