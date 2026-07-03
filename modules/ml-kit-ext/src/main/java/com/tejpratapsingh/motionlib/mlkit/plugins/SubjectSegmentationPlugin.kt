package com.tejpratapsingh.motionlib.mlkit.plugins

import android.graphics.Bitmap
import android.graphics.Color
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.tejpratapsingh.motionlib.core.MotionPlugin
import java.nio.FloatBuffer

/**
 * A [MotionPlugin] that uses ML Kit Subject Segmentation to remove background from a [Bitmap].
 * This processes the bitmap directly and returns a new bitmap with the background removed.
 */
class SubjectSegmentationPlugin : MotionPlugin {
    private val options =
        SubjectSegmenterOptions
            .Builder()
            .enableForegroundConfidenceMask()
            .build()

    private val segmenter = SubjectSegmentation.getClient(options)

    override fun apply(input: Bitmap): Bitmap {
        val image = InputImage.fromBitmap(input, 0)
        return try {
            // This is synchronous and can be slow.
            val result = Tasks.await(segmenter.process(image))
            val mask = result.foregroundConfidenceMask
            if (mask != null) {
                applyMaskToBitmap(input, mask)
            } else {
                input
            }
        } catch (e: Exception) {
            input
        }
    }

    private fun applyMaskToBitmap(
        source: Bitmap,
        mask: FloatBuffer,
    ): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        mask.rewind()
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in 0 until width * height) {
            val confidence = if (mask.hasRemaining()) mask.get() else 0.0f
            val alpha = (Color.alpha(pixels[i]) * confidence).toInt()
            pixels[i] =
                Color.argb(
                    alpha,
                    Color.red(pixels[i]),
                    Color.green(pixels[i]),
                    Color.blue(pixels[i]),
                )
        }

        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
}
