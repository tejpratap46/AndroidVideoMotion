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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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

        coroutineScope {
            val semaphore = Semaphore(4) // Limit parallel storage tasks to avoid OOM
            for (i in 1..totalFrames) {
                Timber.d("produceVideo: frame $i")
                val frameViewBitmap: Bitmap =
                    motionComposerView
                        .forFrame(i)
                        .getViewBitmap()

                launch {
                    semaphore.withPermit {
                        val frameBitmap: Bitmap =
                            frameViewBitmap.compressToBitmap(motionConfig.outputQuality)

                        // Recycle the original bitmap from the view
                        frameViewBitmap.recycle()

                        try {
                            context.saveBitmapToCacheFolder(
                                frameBitmap,
                                subDirName,
                                String.format(Locale.getDefault(), "%05d.png", i),
                            )
                        } catch (e: Exception) {
                            Timber.e(e, "Error saving frame $i: ${e.message}")
                        }

                        progressListener?.let {
                            it(i, frameBitmap)
                        }

                        // Optional: frameBitmap.recycle() if not needed anymore
                    }
                }
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
