package com.tejpratapsingh.motionlib.pytorch.superres

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.scale
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SuperRes(context: Context) {

    private lateinit var module: Module

    companion object {
        private const val MODEL_NAME = "ninasr_b0_2x.ptl"
        private const val INPUT_SIZE = 224
        private const val SCALE_FACTOR = 2
    }

    init {
        try {
            val modelPath = assetFilePath(context, MODEL_NAME)
            module = LiteModuleLoader.load(modelPath)
        } catch (e: IOException) {
            throw RuntimeException("Failed to load super resolution model", e)
        }
    }

    fun upscaleImage(inputBitmap: Bitmap): Bitmap? {
        return try {
            // Preprocess the input image
            val inputTensor = preprocessImage(inputBitmap)

            // Run inference
            val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()

            // Postprocess the output
            postprocessOutput(outputTensor, inputBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun preprocessImage(bitmap: Bitmap): Tensor {
        // Resize image to model input size
        val resizedBitmap = bitmap.scale(INPUT_SIZE, INPUT_SIZE)

        // Convert to tensor with normalization
        return TensorImageUtils.bitmapToFloat32Tensor(
            resizedBitmap,
            floatArrayOf(0.485f, 0.456f, 0.406f), // ImageNet mean
            floatArrayOf(0.229f, 0.224f, 0.225f)  // ImageNet std
        )
    }

    private fun postprocessOutput(outputTensor: Tensor, originalBitmap: Bitmap): Bitmap {
        // Get tensor data
        val outputData = outputTensor.dataAsFloatArray
        val shape = outputTensor.shape()
        val channels = shape[1].toInt() // batch, channels, height, width
        val height = shape[2].toInt()
        val width = shape[3].toInt()

        // Convert tensor to bitmap
        val pixels = IntArray(height * width)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val baseIndex = y * width + x
                val r: Int
                val g: Int
                val b: Int

                if (channels == 1) {
                    // Grayscale: R, G, B are all the same value from the single channel
                    val intensity = (outputData[baseIndex] * 255).coerceIn(0f, 255f).toInt()
                    r = intensity
                    g = intensity
                    b = intensity
                } else if (channels == 3) {
                    // Color: R, G, B from their respective channel planes
                    // Assumes CHW planar format: RRR...GGG...BBB...
                    r = (outputData[baseIndex] * 255).coerceIn(0f, 255f).toInt()
                    g = (outputData[height * width + baseIndex] * 255).coerceIn(0f, 255f).toInt()
                    b = (outputData[2 * height * width + baseIndex] * 255).coerceIn(0f, 255f)
                        .toInt()
                } else {
                    throw IllegalArgumentException("Unsupported number of output channels: $channels. Expected 1 or 3.")
                }

                pixels[baseIndex] = (255 shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    @Throws(IOException::class)
    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }

        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
            return file.absolutePath
        }
    }
}
