package com.tejpratapsingh.motionlib.ffmpeg

import android.content.Context
import android.graphics.Bitmap
import timber.log.Timber
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
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class FfmpegVideoProducerAdapter : VideoProducerAdapter {
    private val subDirName by lazy { UUID.randomUUID().toString() }

    override suspend fun produceVideo(
        context: Context,
        motionComposerViews: List<MotionView>,
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
                        Timber.d("produceVideo: frame $frame")
                        val capturedBitmap = captureFrameBitmap(motionComposerView, frame)
                        val frameBitmap: Bitmap =
                            capturedBitmap.compressToBitmap(motionConfig.outputQuality)

                        // It's good practice to handle potential IOExceptions when saving files
                        try {
                            context.saveBitmapToCacheFolder(
                                frameBitmap,
                                subDirName,
                                String.format(Locale.getDefault(), "%05d.png", frame),
                            )
                        } catch (e: Exception) {
                            Timber.e(e, "Error saving frame $frame: ${e.message}")
                            throw IllegalStateException("Unable to save frame $frame", e)
                        }

                        progressListener?.invoke(frame, frameBitmap)
                    }
                }
            }.awaitAll()
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

        Timber.d("Executing FFmpeg query: $query")
        val session = FFmpegKit.execute(query)

        val returnCode = session.returnCode
        if (ReturnCode.isSuccess(returnCode)) {
            Timber.d("Video created successfully at ${outputFile.path}")
        } else {
            Timber.e("FFmpeg execution failed with return code: $returnCode")
            Timber.e("FFmpeg session logs: ${session.allLogsAsString}") // Crucial for debugging
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
