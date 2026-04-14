package com.tejpratapsingh.motionlib.core.adapter

import android.content.Context
import android.graphics.Bitmap
import timber.log.Timber
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

class AndroidVideoProducerAdapter : VideoProducerAdapter {

    private val subDirName by lazy { UUID.randomUUID().toString() }

    private val androidVideoGenerator = AndroidVideoGenerator()

    override suspend fun produceVideo(
        context: Context,
        motionComposerView: MotionView,
        motionAudio: List<MotionAudio>,
        totalFrames: Int,
        outputFile: File,
        progressListener: (suspend (Int, Bitmap) -> Unit)?,
    ): File {
        Timber.i("produceVideo: starting")
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

        for (i in 1..totalFrames) {
            Timber.d("produceVideo: frame $i")
            val frameBitmap: Bitmap =
                motionComposerView
                    .forFrame(i)
                    .getViewBitmap()
                    .compressToBitmap(motionConfig.outputQuality)

            try {
                context.saveBitmapToCacheFolder(
                    frameBitmap,
                    subDirName,
                    String.format(Locale.getDefault(), "%05d.png", i),
                )
            } catch (e: Exception) {
                Timber.e(e, "Error saving frame $i: ${e.message}")
                // Decide how to handle this error, e.g., stop processing, skip frame, etc.
                return outputFile // Or throw a custom exception
            }

            progressListener?.let {
                it(i, frameBitmap)
            }
        }

        androidVideoGenerator.generateVideo(
            inputDir = subDir,
            motionAudio = motionAudio,
            outputFile = outputFile,
        )

        return outputFile
    }
}
