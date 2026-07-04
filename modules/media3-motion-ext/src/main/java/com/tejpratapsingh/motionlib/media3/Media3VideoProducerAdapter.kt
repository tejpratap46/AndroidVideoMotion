package com.tejpratapsingh.motionlib.media3

import android.content.Context
import android.graphics.Bitmap
import androidx.media3.common.util.UnstableApi
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import java.io.File
import java.util.UUID

/**
 * An implementation of [VideoProducerAdapter] that uses Media3 Transformer for video production.
 * This class orchestrates the video production process by delegating tasks to specialized components.
 */
@UnstableApi
class Media3VideoProducerAdapter(
    private val frameProcessorFactory: (String) -> FrameProcessor = { subDirName -> FrameProcessor(subDirName) },
    private val compositionBuilder: Media3CompositionBuilder = Media3CompositionBuilder(),
    private val transformerRunner: Media3TransformerRunner = Media3TransformerRunner(),
) : VideoProducerAdapter {
    private val subDirName by lazy { UUID.randomUUID().toString() }

    override suspend fun produceVideo(
        context: Context,
        motionComposerView: MotionView,
        motionAudio: List<MotionAudio>,
        totalFrames: Int,
        outputFile: File,
        progressListener: (suspend (Int, Bitmap) -> Unit)?,
    ): File {
        if (outputFile.exists()) {
            outputFile.delete()
        }

        val subDir = File(context.cacheDir, subDirName)
        if (subDir.exists()) {
            subDir.deleteRecursively()
        }
        subDir.mkdirs()

        val motionConfig: MotionConfig = provideCurrentConfig()
        val frameProcessor = frameProcessorFactory(subDirName)

        try {
            // S: Frame generation responsibility delegated to FrameProcessor
            frameProcessor.writeFramesToCache(
                context = context,
                motionComposerView = motionComposerView,
                motionConfig = motionConfig,
                totalFrames = totalFrames,
                progressListener = progressListener,
            )

            // S: Composition building responsibility delegated to Media3CompositionBuilder
            val composition =
                compositionBuilder.build(
                    frameDirectory = subDir,
                    motionAudio = motionAudio,
                    motionConfig = motionConfig,
                )

            // S: Transformer execution responsibility delegated to Media3TransformerRunner
            transformerRunner.export(
                context = context,
                composition = composition,
                outputFile = outputFile,
            )
        } finally {
            if (subDir.exists()) {
                subDir.deleteRecursively()
            }
        }

        return outputFile
    }
}
