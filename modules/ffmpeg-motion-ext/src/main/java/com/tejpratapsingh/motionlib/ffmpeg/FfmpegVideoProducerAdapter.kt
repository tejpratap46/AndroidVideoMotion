package com.tejpratapsingh.motionlib.ffmpeg

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.core.extensions.compressToBitmap
import com.tejpratapsingh.motionlib.core.extensions.saveBitmapToCacheFolder
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import java.io.File
import java.util.Locale

class FfmpegVideoProducerAdapter : VideoProducerAdapter {
    companion object {
        private const val TAG = "FfmpegVideoProducerAdap"
    }

    private val subDirName = "motion_frames"

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

        // Ensure the cache subdirectory exists and is empty before saving new frames
        val subDir = File(context.cacheDir, subDirName)
        if (subDir.exists()) {
            subDir.deleteRecursively() // Clear old frames
        }
        subDir.mkdirs() // Create the directory if it doesn't exist

        val motionConfig: MotionConfig = provideCurrentConfig()

        for (i in 1..totalFrames) {
            Log.d(TAG, "produceVideo: frame $i")
            val frameBitmap: Bitmap =
                motionComposerView
                    .forFrame(i)
                    .getViewBitmap()
                    .compressToBitmap(motionConfig.outputQuality)

            // It's good practice to handle potential IOExceptions when saving files
            try {
                context.saveBitmapToCacheFolder(
                    frameBitmap,
                    subDirName,
                    String.format(Locale.getDefault(), "%05d.png", i),
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

        val inputPattern = "${subDir.path}/%05d.png"

        // -y: Overwrite output files without asking
        // -framerate: Input framerate for the image sequence
        // -start_number 1: Explicitly tell FFmpeg to start numbering from 1 (if your files are 001.png, 002.png, etc.)
        //                  If your files start from 000.png, you can omit this or set it to 0.
        // -i: Input file pattern
        // -c:v libx264: Video codec (H.264)
        // -pix_fmt yuv420p: Pixel format, good for compatibility
        // -r: Output framerate (often the same as input, but can be different)
//        val query = "-y -framerate ${motionConfig.fps} -start_number 1 -i \"$inputPattern\" -c:v libx264 -pix_fmt yuv420p -r ${motionConfig.fps} \"${outputFile.path}\""

        val query =
            buildFfmpegCommand(
                inputPattern = inputPattern,
                fps = motionConfig.fps,
                outputFile = outputFile,
                audioTracks = motionAudio,
                startNumber = 1,
                mixAudio = true, // Change to false if you want separate audio tracks
            ).joinToString(" ")

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

    fun buildFfmpegCommand(
        inputPattern: String, // e.g. "/sdcard/frames/frame_%d.png"
        fps: Int,
        outputFile: File,
        audioTracks: List<MotionAudio>,
        startNumber: Int = 1,
        mixAudio: Boolean = true, // true = mix tracks, false = keep separate
    ): List<String> {
        val command = mutableListOf<String>()

        // Base video input (image sequence)
        command.addAll(
            listOf(
                "-y",
                "-framerate",
                fps.toString(),
                "-start_number",
                startNumber.toString(),
                "-i",
                inputPattern,
            ),
        )

        // Add audio inputs
        audioTracks.forEach { track ->
            command.addAll(listOf("-i", track.file.absolutePath))
        }

        if (mixAudio && audioTracks.isNotEmpty()) {
            // Build filter_complex for trimming & mixing
            val filterParts = mutableListOf<String>()
            audioTracks.forEachIndexed { index, track ->
                val startSec = track.startFrame / fps
                val endSec = track.endFrame / fps
                val delayMs = (track.delayFrame / fps * 1000)

                val label = "a${index + 1}"
                filterParts.add(
                    "[${index + 1}:a]atrim=start=$startSec:end=$endSec," +
                        "asetpts=PTS-STARTPTS," +
                        "adelay=$delayMs|$delayMs[$label]",
                )
            }

            val labels = audioTracks.mapIndexed { index, _ -> "[a${index + 1}]" }
            filterParts.add("${labels.joinToString("")}amix=inputs=${audioTracks.size}:normalize=0[outa]")

            command.addAll(listOf("-filter_complex", filterParts.joinToString(";")))

            // Map video + mixed audio
            command.addAll(
                listOf(
                    "-map",
                    "0:v",
                    "-map",
                    "[outa]",
                    "-c:v",
                    "libx264",
                    "-pix_fmt",
                    "yuv420p",
                    "-r",
                    fps.toString(),
                    "-shortest",
                    outputFile.absolutePath,
                ),
            )
        } else {
            // Keep tracks separate, no filter_complex
            command.addAll(
                listOf("-c:v", "libx264", "-pix_fmt", "yuv420p", "-r", fps.toString()),
            )

            // Map video and each audio
            command.add("-map")
            command.add("0:v")
            audioTracks.forEachIndexed { index, _ ->
                command.addAll(listOf("-map", "${index + 1}:a"))
            }

            command.addAll(listOf("-shortest", outputFile.absolutePath))
        }

        return command
    }

    fun frameToSeconds(
        frame: Int,
        fps: Int,
    ): Double = frame.toDouble() / fps.toDouble()
}
