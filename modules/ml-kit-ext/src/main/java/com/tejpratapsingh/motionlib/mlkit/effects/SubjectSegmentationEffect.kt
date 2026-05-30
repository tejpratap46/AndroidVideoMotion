package com.tejpratapsingh.motionlib.mlkit.effects

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import android.view.View
import androidx.core.graphics.createBitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import java.nio.FloatBuffer

/**
 * A [MotionEffect] that uses ML Kit Subject Segmentation to remove background.
 * Works only on Android T (API 33) and above due to [RenderEffect] and [RuntimeShader].
 */
class SubjectSegmentationEffect(
    override val startFrame: Int,
    override val endFrame: Int,
) : MotionEffect {
    override lateinit var motionView: MotionView

    private val options =
        SubjectSegmenterOptions
            .Builder()
            .enableForegroundConfidenceMask()
            .build()

    private val segmenter = SubjectSegmentation.getClient(options)

    private val maskCache = mutableMapOf<Int, Bitmap>()

    @Suppress("ktlint:standard:property-naming")
    private val MASK_SHADER =
        """
        uniform shader content;
        uniform shader mask;

        half4 main(float2 fragCoord) {
            half4 color = content.eval(fragCoord);
            half4 maskColor = mask.eval(fragCoord);
            // Use alpha channel of the mask for transparency
            return color * maskColor.a;
        }
        """.trimIndent()

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView
        val view = motionView as View

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return motionView

        if (frame !in startFrame..endFrame) {
            // If we are past the end frame, clear the effect
            if (frame > endFrame) {
                view.setRenderEffect(null)
            }
            return motionView
        }

        val maskBitmap =
            maskCache[frame] ?: run {
                val bitmap = motionView.getViewBitmap()
                val image = InputImage.fromBitmap(bitmap, 0)
                try {
                    // This is synchronous and can be slow.
                    // Ideally, this should be pre-processed or run on a background thread.
                    val result = Tasks.await(segmenter.process(image))
                    val mask = result.foregroundConfidenceMask // This is a FloatBuffer
                    if (mask != null) {
                        val createdMask = createBitmapFromMask(mask, bitmap.width, bitmap.height)
                        maskCache[frame] = createdMask
                        createdMask
                    } else {
                        null
                    }
                } catch (_: Exception) {
                    null
                }
            }

        if (maskBitmap != null) {
            val shader = RuntimeShader(MASK_SHADER)
            val maskShader = BitmapShader(maskBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            shader.setInputShader("mask", maskShader)

            view.setRenderEffect(RenderEffect.createRuntimeShaderEffect(shader, "content"))
        }

        return motionView
    }

    private fun createBitmapFromMask(
        mask: FloatBuffer,
        width: Int,
        height: Int,
    ): Bitmap {
        val bitmap = createBitmap(width, height)
        mask.rewind()
        val pixels = IntArray(width * height)
        for (i in 0 until width * height) {
            val confidence = if (mask.hasRemaining()) mask.get() else 0.0f
            val alpha = (confidence * 255).toInt()
            // Set alpha for the mask bitmap, color doesn't strictly matter if we only use alpha in shader
            pixels[i] = Color.argb(alpha, 255, 255, 255)
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}
