package com.tejpratapsingh.motionlib.ffmpeg

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.tejpratapsingh.motionlib.core.IMotionView
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.core.extensions.compressToBitmap
import com.tejpratapsingh.motionlib.core.extensions.getViewBitmap
import java.io.File
import java.util.Locale

class FfmpegVideoProducerAdapter : VideoProducerAdapter {

    companion object {
        private const val TAG = "FfmpegVideoProducerAdap"
    }

    lateinit var context: Context
    private val subDirName = "motion_frames"

    override suspend fun produceVideo(
        motionConfig: MotionConfig,
        motionComposerView: IMotionView,
        totalFrames: Int,
        outputFile: File,
        progressListener: ((Int, Bitmap) -> Unit)?
    ): File {

        if (!::context.isInitialized) {
            val errorMessage = "applicationContext not initialised"
            Log.e(
                TAG,
                "produceVideo: $errorMessage",
                IllegalStateException(errorMessage)
            )
            return outputFile // Or throw the exception
        }


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
            val frameBitmap: Bitmap = motionComposerView.forFrame(i).getViewBitmap()
                .compressToBitmap(motionConfig.outputQuality)

            // It's good practice to handle potential IOExceptions when saving files
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

        val inputPattern = "${subDir.path}/%03d.png"

        // -y: Overwrite output files without asking
        // -framerate: Input framerate for the image sequence
        // -start_number 1: Explicitly tell FFmpeg to start numbering from 1 (if your files are 001.png, 002.png, etc.)
        //                  If your files start from 000.png, you can omit this or set it to 0.
        // -i: Input file pattern
        // -c:v libx264: Video codec (H.264)
        // -pix_fmt yuv420p: Pixel format, good for compatibility
        // -r: Output framerate (often the same as input, but can be different)
        val query = "-y -framerate ${motionConfig.fps} -start_number 1 -i \"$inputPattern\" -c:v libx264 -pix_fmt yuv420p -r ${motionConfig.fps} \"${outputFile.path}\""

        Log.d(TAG, "Executing FFmpeg query: $query")
        val session = FFmpegKit.execute(query)

        val returnCode = session.returnCode
        if (ReturnCode.isSuccess(returnCode)) {
            Log.d(TAG, "Video created successfully at ${outputFile.path}")
        } else {
            Log.e(TAG, "FFmpeg execution failed with return code: $returnCode")
            Log.e(TAG, "FFmpeg session logs: ${session.allLogsAsString}") // Crucial for debugging
            // Consider deleting the partially created (or empty) output file on failure
            if (outputFile.exists()) {
                outputFile.delete()
            }
        }

        // Clean up the cache directory after video generation (optional, but good practice)
        if (subDir.exists()) {
            subDir.deleteRecursively()
        }

        return outputFile
    }
}
