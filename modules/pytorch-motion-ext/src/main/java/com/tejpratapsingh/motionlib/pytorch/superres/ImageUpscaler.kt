package com.tejpratapsingh.motionlib.pytorch.superres

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.scale
import com.tejpratapsingh.motionlib.pytorch.common.ModelTypes
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageUpscaler(context: Context) {

    private var module: Module

    companion object {
        private val MODEL_NAME = ModelTypes.NINASR.fileName
        private const val INPUT_SIZE = 224 // IMPORTANT: Verify this for ninasr_b0_2x.ptl
    }

    init {
        try {
            val modelPath = assetFilePath(context, MODEL_NAME)
            module = LiteModuleLoader.load(modelPath)
        } catch (e: IOException) {
            throw RuntimeException(
                "Failed to load super resolution model: $MODEL_NAME. Ensure it's in app/src/main/assets/",
                e
            )
        }
    }

    fun upscaleImage(inputBitmap: Bitmap): Bitmap? {
        return try {
            val inputTensor = preprocessImage(inputBitmap)
            val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
            postprocessOutput(outputTensor)
        } catch (e: Exception) {
            e.printStackTrace() // Consider using a proper logger
            null
        }
    }

    private fun preprocessImage(bitmap: Bitmap): Tensor {
        val resizedBitmap = bitmap.scale(INPUT_SIZE, INPUT_SIZE)
        // IMPORTANT: Verify these normalization values for ninasr_b0_2x.ptl
        return TensorImageUtils.bitmapToFloat32Tensor(
            resizedBitmap,
            floatArrayOf(0.485f, 0.456f, 0.406f), // Example: ImageNet mean
            floatArrayOf(0.229f, 0.224f, 0.225f)  // Example: ImageNet std
        )
    }

    private fun postprocessOutput(outputTensor: Tensor): Bitmap {
        val outputData = outputTensor.dataAsFloatArray
        val shape = outputTensor.shape() // Expected: [batch, channels, height, width]

        // Assuming batch size is 1
        val channels = shape[1].toInt()
        val height = shape[2].toInt()
        val width = shape[3].toInt()

        val pixels = IntArray(height * width)

        // IMPORTANT: This conversion logic assumes a certain output format from the model.
        // Verify if ninasr_b0_2x.ptl outputs in CHW planar format and if values are normalized (e.g., [0,1]).
        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                val r: Int
                val g: Int
                val b: Int

                if (channels == 1) { // Grayscale
                    val intensity = (outputData[idx].coerceIn(0f, 1f) * 255).toInt()
                    r = intensity
                    g = intensity
                    b = intensity
                } else if (channels == 3) { // Color
                    // Assuming CHW planar: RRR...GGG...BBB...
                    // And output values are in [0,1] range or similar normalized range.
                    val rNorm = outputData[idx]
                    val gNorm = outputData[height * width + idx]
                    val bNorm = outputData[2 * height * width + idx]

                    r = (rNorm.coerceIn(0f, 1f) * 255).toInt()
                    g = (gNorm.coerceIn(0f, 1f) * 255).toInt()
                    b = (bNorm.coerceIn(0f, 1f) * 255).toInt()
                } else {
                    throw IllegalArgumentException("Unsupported number of output channels: $channels. Expected 1 or 3.")
                }
                pixels[idx] = (255 shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    @Throws(IOException::class)
    private fun assetFilePath(context: Context, assetName: String): String {
        val assetFile = File(context.filesDir, assetName)
        if (assetFile.exists() && assetFile.length() > 0) {
            return assetFile.absolutePath
        }

        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(assetFile).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
        }
        return assetFile.absolutePath
    }
}