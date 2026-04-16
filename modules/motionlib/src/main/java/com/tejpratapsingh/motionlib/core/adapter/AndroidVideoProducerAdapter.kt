package com.tejpratapsingh.motionlib.core.adapter

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.core.extensions.compressToBitmap
import com.tejpratapsingh.motionlib.core.extensions.saveBitmapToCacheFolder
import com.tejpratapsingh.motionlib.core.infra.AndroidVideoGenerator
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class AndroidVideoProducerAdapter : VideoProducerAdapter {
    companion object {
        private const val TAG = "AndroidVideoProducerAda"
    }

    private val subDirName by lazy { UUID.randomUUID().toString() }

    private val androidVideoGenerator = AndroidVideoGenerator()

    override suspend fun produceVideo(
        context: Context,
        motionComposerViews: List<MotionView>,
        motionAudio: List<MotionAudio>,
        totalFrames: Int,
        outputFile: File,
        progressListener: (suspend (Int, Bitmap) -> Unit)?,
    ): File {
        Log.i(TAG, "produceVideo: starting")
        if (outputFile.exists()) {
            outputFile.delete()
        }
        // Ensure the cache subdirectory exists and is empty before saving new frames
        val subDir = File(context.cacheDir, subDirName)
        if (subDir.exists()) {
            subDir.deleteRecursively() // Clear old frames
        }
        subDir.mkdirs() // Create the directory if it doesn't exist

        val motionConfig: MotionConfig = provideCurrentConfig()
        val safeMotionComposerViews = motionComposerViews.ifEmpty { error("At least one MotionView is required") }
        val workerCount =
            minOf(
                totalFrames.coerceAtLeast(1),
                safeMotionComposerViews.size.coerceAtLeast(1),
                Runtime.getRuntime().availableProcessors().coerceAtLeast(1),
            )
        val chunkSize = ((totalFrames + workerCount) - 1) / workerCount

        coroutineScope {
            (0 until workerCount).map { workerIndex ->
                async(Dispatchers.Default) {
                    val motionComposerView = safeMotionComposerViews[workerIndex % safeMotionComposerViews.size]
                    val startFrame = (workerIndex * chunkSize) + 1
                    val endFrame = minOf(totalFrames, startFrame + chunkSize - 1)

                    for (frame in startFrame..endFrame) {
                        Log.d(TAG, "produceVideo: frame $frame")
                        val capturedBitmap = captureFrameBitmap(motionComposerView, frame)
                        val frameBitmap: Bitmap =
                            capturedBitmap.compressToBitmap(motionConfig.outputQuality)

                        try {
                            context.saveBitmapToCacheFolder(
                                frameBitmap,
                                subDirName,
                                String.format(Locale.getDefault(), "%05d.png", frame),
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error saving frame $frame: ${e.message}", e)
                            throw IllegalStateException("Unable to save frame $frame", e)
                        }

                        progressListener?.invoke(frame, frameBitmap)
                    }
                }
            }.awaitAll()
        }

        androidVideoGenerator.generateVideo(
            inputDir = subDir,
            motionAudio = motionAudio,
            outputFile = outputFile,
        )

        return outputFile
    }

    private fun captureFrameBitmap(
        motionComposerView: MotionView,
        frame: Int,
    ): Bitmap =
        synchronized(motionComposerView) {
            motionComposerView
                .forFrame(frame)
                .getViewBitmap()
        }
}
