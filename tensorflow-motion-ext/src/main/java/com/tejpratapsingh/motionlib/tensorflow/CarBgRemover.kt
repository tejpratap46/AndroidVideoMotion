package com.tejpratapsingh.motionlib.tensorflow

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.core.graphics.set
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class CarBgRemover(context: Context) {

    private val interpreter: Interpreter

    init {
        interpreter = Interpreter(loadModelFile(context, "deeplabv3_257_mv_gpu.tflite"))
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun removeBackground(
        bitmap: Bitmap,
        modelInputWidth: Int? = null,
        modelInputHeight: Int? = null
    ): Bitmap {
        val inputW = modelInputWidth ?: bitmap.width
        val inputH = modelInputHeight ?: bitmap.height

        val resizedBitmap = bitmap.scale(inputW, inputH)
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

        val output = Array(1) { Array(inputH) { Array(inputW) { FloatArray(1) } } }
        interpreter.run(inputBuffer, output)

        return createMaskedBitmap(resizedBitmap, output[0])
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val w = bitmap.width
        val h = bitmap.height
        val input = Array(1) { Array(h) { Array(w) { FloatArray(3) } } }

        for (y in 0 until h) {
            for (x in 0 until w) {
                val px = bitmap[x, y]
                input[0][y][x][0] = (Color.red(px) - 127.5f) / 127.5f
                input[0][y][x][1] = (Color.green(px) - 127.5f) / 127.5f
                input[0][y][x][2] = (Color.blue(px) - 127.5f) / 127.5f
            }
        }
        return input
    }

    private fun createMaskedBitmap(original: Bitmap, mask: Array<Array<FloatArray>>): Bitmap {
        val w = original.width
        val h = original.height
        val scaledMask = createBitmap(w, h)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val v = mask[y][x][0]
                val alpha = if (v > 0.5f) 255 else 0
                scaledMask[x, y] = Color.argb(alpha, 255, 255, 255)
            }
        }

        val result = createBitmap(w, h)
        val canvas = Canvas(result)
        val paint = Paint().apply { isFilterBitmap = true }

        canvas.drawBitmap(original, 0f, 0f, paint)

        val porterPaint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        canvas.drawBitmap(scaledMask, 0f, 0f, porterPaint)

        return result
    }
}
