package com.tejpratapsingh.motionlib.tensorflow.superres

import android.content.Context
import android.graphics.Bitmap
import com.tejpratapsingh.motionlib.tensorflow.ImageUtils
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil

class SuperResolutionProcessor(
    context: Context,
    modelFile: String
) {
    private val interpreter: Interpreter

    init {
        val model = FileUtil.loadMappedFile(context, modelFile)
        interpreter = Interpreter(model)
    }

    fun enhance(bitmap: Bitmap): Bitmap {
        val inputWidth = bitmap.width
        val inputHeight = bitmap.height

        // Convert input bitmap to normalized float buffer
        val inputBuffer = ImageUtils.bitmapToFloatBuffer(bitmap)

        // Model should upscale by 2x → output size
        val outputWidth = inputWidth * 2
        val outputHeight = inputHeight * 2

        // Create output buffer shape: [1, height*2, width*2, 3]
        val outputBuffer = Array(1) {
            Array(outputHeight) {
                Array(outputWidth) {
                    FloatArray(3)
                }
            }
        }

        // Resize input tensor before running, if model allows dynamic input
        interpreter.resizeInput(0, intArrayOf(1, inputHeight, inputWidth, 3))
        interpreter.allocateTensors()

        // Run inference
        interpreter.run(inputBuffer, outputBuffer)

        // Convert output float array to Bitmap
        return ImageUtils.floatArrayToBitmap(outputBuffer[0])
    }
}