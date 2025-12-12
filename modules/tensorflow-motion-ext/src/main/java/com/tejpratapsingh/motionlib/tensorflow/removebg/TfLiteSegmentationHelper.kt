package com.tejpratapsingh.motionlib.tensorflow.removebg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class TfLiteSegmentationHelper(
    context: Context,
) {
    private val modelName = "deeplabv3_257_mv_gpu.tflite"
    private val inputSize = 257
    private val interpreter: Interpreter

    init {
        val modelFile = FileUtil.loadMappedFile(context, modelName)
        interpreter = Interpreter(modelFile)
    }

    fun segmentAndRemoveBackground(bitmap: Bitmap): Bitmap {
        // Run segmentation on background thread
        val mask = runDeepLabSegmentation(bitmap)
        return applyMask(bitmap, mask)
    }

    private fun runDeepLabSegmentation(originalBitmap: Bitmap): Bitmap {
        // 1. Resize input
        val resized = originalBitmap.scale(inputSize, inputSize)

        // 2. Prepare input tensor
        val tensorImage = TensorImage.fromBitmap(resized)
        val processor =
            ImageProcessor
                .Builder()
                .add(NormalizeOp(127.5f, 127.5f))
                .build()
        val input = processor.process(tensorImage)

        // 3. Prepare output buffer
        val outputBuffer =
            TensorBuffer.createFixedSize(
                intArrayOf(1, inputSize, inputSize, 21),
                org.tensorflow.lite.DataType.FLOAT32,
            )

        // 4. Run inference
        interpreter.run(input.buffer, outputBuffer.buffer.rewind())

        // 5. Build mask bitmap
        val maskPixels = IntArray(inputSize * inputSize)
        val scores = outputBuffer.floatArray
        for (i in maskPixels.indices) {
            val offset = i * 21
            val maxIndex =
                scores
                    .copyOfRange(offset, offset + 21)
                    .withIndex()
                    .maxByOrNull { it.value }!!
                    .index
            maskPixels[i] = if (maxIndex == 15) Color.WHITE else Color.TRANSPARENT
        }
        val mask = Bitmap.createBitmap(maskPixels, inputSize, inputSize, Bitmap.Config.ARGB_8888)
        return mask.scale(originalBitmap.width, originalBitmap.height)
    }

    private fun applyMask(
        original: Bitmap,
        mask: Bitmap,
    ): Bitmap {
        val result = createBitmap(original.width, original.height)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(original, 0f, 0f, null)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawBitmap(mask, 0f, 0f, paint)
        paint.xfermode = null
        return result
    }
}
