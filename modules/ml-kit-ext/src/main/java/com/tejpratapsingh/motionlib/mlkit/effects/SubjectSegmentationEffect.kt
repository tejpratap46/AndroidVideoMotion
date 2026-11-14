package com.tejpratapsingh.motionlib.mlkit.effects

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import android.os.Looper
import android.view.View
import androidx.core.graphics.createBitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import java.nio.FloatBuffer

/**
 * A [MotionEffect] that uses ML Kit Subject Segmentation to remove background.
 * Works only on Android T (API 33) and above due to [RenderEffect] and [RuntimeShader].
 */
class SubjectSegmentationEffect @JvmOverloads constructor(
    override val startFrame: Int,
    override val endFrame: Int,
    var isExport: Boolean? = null,
    internal val segmenter: SubjectSegmenter =
        SubjectSegmentation.getClient(
            SubjectSegmenterOptions
                .Builder()
                .enableForegroundConfidenceMask()
                .build(),
        ),
    internal val imageConverter: (Bitmap) -> InputImage = { bitmap ->
        InputImage.fromBitmap(bitmap, 0)
    },
) : MotionEffect {
    override lateinit var motionView: MotionView

    private val lock = Any()
    internal val maskCache = mutableMapOf<Int, Bitmap>()

    // Preview state: bounded latest-frame policy
    internal var isProcessing = false
        private set
    internal var inFlightFrame: Int? = null
        private set
    internal var pendingLatestFrame: Int? = null
        private set
    internal var pendingLatestBitmap: Bitmap? = null
        private set
    private var currentFrame: Int = -1

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

        currentFrame = frame

        if (frame !in startFrame..endFrame) {
            // If we are past the end frame, clear the effect
            if (frame > endFrame) {
                view.setRenderEffect(null)
            }
            clearPendingPreview()
            return motionView
        }

        val maskBitmap = synchronized(lock) { maskCache[frame] }
        if (maskBitmap != null) {
            clearPendingPreview()
            applyMaskToView(view, maskBitmap)
            return motionView
        }

        if (isExportMode()) {
            processExportFrame(frame, view)
        } else {
            submitPreviewFrame(frame, view)
        }

        return motionView
    }

    private fun isExportMode(): Boolean {
        isExport?.let { return it }
        return try {
            val looper = Looper.myLooper()
            looper == null || looper != Looper.getMainLooper()
        } catch (_: Exception) {
            false
        }
    }

    private fun applyMaskToView(
        view: View,
        maskBitmap: Bitmap,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shader = RuntimeShader(MASK_SHADER)
            val maskShader = BitmapShader(maskBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            shader.setInputShader("mask", maskShader)

            view.setRenderEffect(RenderEffect.createRuntimeShaderEffect(shader, "content"))
        }
    }

    private fun clearPendingPreview() {
        val oldBitmap =
            synchronized(lock) {
                val bitmap = pendingLatestBitmap
                pendingLatestBitmap = null
                pendingLatestFrame = null
                bitmap
            }
        oldBitmap?.recycle()
    }

    /**
     * Synchronous backpressure processing for video export.
     * Blocks the calling export thread until segmentation is complete, ensuring exactly one in-flight
     * segmentation task and immediate availability of the mask bitmap for rendering.
     */
    private fun processExportFrame(
        frame: Int,
        view: View,
    ) {
        val bitmap = motionView.getViewBitmap()
        if (bitmap.width <= 0 || bitmap.height <= 0) {
            bitmap.recycle()
            return
        }
        val image = imageConverter(bitmap)
        try {
            val task = segmenter.process(image)
            val result =
                if (task.isComplete) {
                    if (task.isSuccessful) task.result else null
                } else {
                    Tasks.await(task)
                }
            val mask = result?.foregroundConfidenceMask
            if (mask != null) {
                val createdMask = createBitmapFromMask(mask, bitmap.width, bitmap.height)
                synchronized(lock) {
                    maskCache[frame] = createdMask
                }
                applyMaskToView(view, createdMask)
            }
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (_: Exception) {
            // Segmentation failed; view continues without mask
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Bounded latest-frame submission flow for preview.
     * Guarantees at most 1 in-flight segmentation request. Intermediate frames are dropped
     * while inference is busy, retaining only the latest requested frame and recycling discarded bitmaps.
     */
    private fun submitPreviewFrame(
        frame: Int,
        view: View,
    ) {
        val shouldProcessImmediately: Boolean
        val oldPendingBitmap: Bitmap?

        synchronized(lock) {
            if (maskCache.containsKey(frame)) {
                return
            }
            if (inFlightFrame == frame) {
                // Current frame is already in flight; discard obsolete pending frame
                val old = pendingLatestBitmap
                pendingLatestBitmap = null
                pendingLatestFrame = null
                old?.recycle()
                return
            }
            if (pendingLatestFrame == frame) {
                // Current frame is already queued as the next latest frame
                return
            }

            if (!isProcessing) {
                isProcessing = true
                inFlightFrame = frame
                shouldProcessImmediately = true
                oldPendingBitmap = null
            } else {
                shouldProcessImmediately = false
                oldPendingBitmap = pendingLatestBitmap
                pendingLatestFrame = frame
            }
        }
        oldPendingBitmap?.recycle()

        val capturedBitmap = motionView.getViewBitmap()
        if (capturedBitmap.width <= 0 || capturedBitmap.height <= 0) {
            capturedBitmap.recycle()
            if (shouldProcessImmediately) {
                onPreviewFrameProcessed(view)
            }
            return
        }

        if (shouldProcessImmediately) {
            processPreviewImage(frame, capturedBitmap, view)
        } else {
            var toRecycle: Bitmap? = null
            synchronized(lock) {
                if (pendingLatestFrame == frame) {
                    pendingLatestBitmap = capturedBitmap
                } else {
                    toRecycle = capturedBitmap
                }
            }
            toRecycle?.recycle()
        }
    }

    private fun processPreviewImage(
        frame: Int,
        bitmap: Bitmap,
        view: View,
    ) {
        try {
            val image = imageConverter(bitmap)
            segmenter
                .process(image)
                .addOnSuccessListener { result ->
                    val mask = result.foregroundConfidenceMask
                    if (mask != null) {
                        val createdMask = createBitmapFromMask(mask, bitmap.width, bitmap.height)
                        synchronized(lock) {
                            maskCache[frame] = createdMask
                        }
                        view.post {
                            if (currentFrame == frame) {
                                applyMaskToView(view, createdMask)
                            }
                            view.invalidate()
                        }
                    }
                    bitmap.recycle()
                    onPreviewFrameProcessed(view)
                }.addOnFailureListener {
                    bitmap.recycle()
                    onPreviewFrameProcessed(view)
                }
        } catch (_: Exception) {
            bitmap.recycle()
            onPreviewFrameProcessed(view)
        }
    }

    private fun onPreviewFrameProcessed(view: View) {
        var nextFrame: Int?
        var nextBitmap: Bitmap?

        while (true) {
            synchronized(lock) {
                nextFrame = pendingLatestFrame
                nextBitmap = pendingLatestBitmap
                pendingLatestFrame = null
                pendingLatestBitmap = null

                if (nextFrame == null || nextBitmap == null) {
                    isProcessing = false
                    inFlightFrame = null
                    return
                }

                inFlightFrame = nextFrame
            }

            val targetFrame = nextFrame ?: return
            val targetBitmap = nextBitmap ?: return

            val alreadyCached = synchronized(lock) { maskCache.containsKey(targetFrame) }
            if (alreadyCached || targetBitmap.width <= 0 || targetBitmap.height <= 0) {
                targetBitmap.recycle()
                continue
            }

            processPreviewImage(targetFrame, targetBitmap, view)
            break
        }
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

    fun clearCache() {
        synchronized(lock) {
            maskCache.values.forEach { it.recycle() }
            maskCache.clear()
            pendingLatestBitmap?.recycle()
            pendingLatestBitmap = null
            pendingLatestFrame = null
            inFlightFrame = null
            isProcessing = false
        }
    }
}
