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
import java.io.File
import java.util.Locale

class AndroidVideoProducerAdapter : VideoProducerAdapter {

    companion object {
        private const val TAG = "AndroidVideoProducerAda"
    }

    private val subDirName = "motion_frames"

    private val androidVideoGenerator = AndroidVideoGenerator()

    override suspend fun produceVideo(
        context: Context,
        motionConfig: MotionConfig,
        motionComposerView: MotionView,
        motionAudio: List<MotionAudio>,
        totalFrames: Int,
        outputFile: File,
        progressListener: ((Int, Bitmap) -> Unit)?
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

        for (i in 1..totalFrames) {
            Log.d(TAG, "produceVideo: frame $i")
            val frameBitmap: Bitmap =
                motionComposerView.forFrame(i).getViewBitmap()
                    .compressToBitmap(motionConfig.outputQuality)

            try {
                context.saveBitmapToCacheFolder(
                    frameBitmap, subDirName, String.format(Locale.getDefault(), "%03d.png", i)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error saving frame $i: ${e.message}", e)
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
            motionConfig = motionConfig
        )

        return outputFile
    }
}