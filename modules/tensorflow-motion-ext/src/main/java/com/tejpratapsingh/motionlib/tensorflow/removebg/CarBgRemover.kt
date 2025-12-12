package com.tejpratapsingh.motionlib.tensorflow.removebg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.set
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class CarBgRemover(
    context: Context,
) {
    companion object {
        private const val MODEL_FILENAME = "DeepLabV3-Plus-MobileNet.tflite"
        private const val PERSON_CLASS_INDEX = 15 // PASCAL VOC “person” index
        private const val FOREGROUND_THRESHOLD = 0.5f
    }

    // Lazy-load the TFLite interpreter
    private val interpreter: Interpreter by lazy {
        Interpreter(loadModelFile(context, MODEL_FILENAME))
    }

    private fun loadModelFile(
        context: Context,
        modelName: String,
    ): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Dynamically determine input size from the model
    private val inputShape: IntArray by lazy {
        interpreter.getInputTensor(0).shape() // e.g. [1, H, W, 3]
    }
    private val inputHeight: Int by lazy { inputShape[1] }
    private val inputWidth: Int by lazy { inputShape[2] }

    // Preprocessor: resize + normalize to [-1,1]
    private val preprocessor: ImageProcessor by lazy {
        ImageProcessor
            .Builder()
            .add(ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(127.5f, 127.5f))
            .build()
    }

    /**
     * Removes background from the given bitmap.
     */
    fun removeBackground(original: Bitmap): Bitmap {
        // 1. Preprocess
        val tensorImage = TensorImage.fromBitmap(original)
        val processedImage = preprocessor.process(tensorImage)
        val inputBuffer = processedImage.buffer

        // 2. Prepare output buffer
        val outputTensor = interpreter.getOutputTensor(0)
        val outputShape = outputTensor.shape() // [1, H_out, W_out, C]
        val outputDataType = outputTensor.dataType()
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType)

        // 3. Run inference
        interpreter.run(inputBuffer, outputBuffer.buffer.rewind())

        // 4. Extract “person” mask channel
        val logits = outputBuffer.floatArray // length = H_out * W_out * C
        val maskWidth = outputShape[2]
        val maskHeight = outputShape[1]
        val mask = FloatArray(maskWidth * maskHeight)
        val stride = outputShape[3] // number of classes

        for (i in mask.indices) {
            mask[i] = logits[i * stride + PERSON_CLASS_INDEX]
        }

        // 5. Apply mask to original bitmap
        return applyMaskToBitmap(original, mask, maskWidth, maskHeight)
    }

    private fun applyMaskToBitmap(
        source: Bitmap,
        mask: FloatArray,
        maskWidth: Int,
        maskHeight: Int,
    ): Bitmap {
        val w = source.width
        val h = source.height
        val result = createBitmap(w, h)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val mx = x * maskWidth / w
                val my = y * maskHeight / h
                val idx = my * maskWidth + mx
                val pixel = source[x, y]
                result[x, y] =
                    if (mask[idx] > FOREGROUND_THRESHOLD) {
                        pixel
                    } else {
                        Color.TRANSPARENT
                    }
            }
        }
        return result
    }

    /** Release interpreter resources. */
    fun close() {
        interpreter.close()
    }
}
