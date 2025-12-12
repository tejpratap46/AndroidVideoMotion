package com.tejpratapsingh.motionlib.ffmpeg.utils

import android.content.Context
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File

fun extractFramesFromVideo(
    context: Context,
    videoFile: File,
    outputDirName: String = "frames",
): String {
    val outputDir = File(context.cacheDir, outputDirName)
    if (!outputDir.exists()) outputDir.mkdirs()

    // Output path pattern for frames: frame_0001.png, frame_0002.png, ...
    val outputPattern = File(outputDir, "frame_%05d.png").absolutePath

    // FFmpeg command: extract every frame
    val cmd = "-i \"${videoFile.absolutePath}\" \"$outputPattern\""

    FFmpegKit.executeAsync(cmd) { session ->
        val returnCode = session.returnCode
        if (ReturnCode.isSuccess(returnCode)) {
            // Success
            println("✅ Frames extracted to: ${outputDir.absolutePath}")
        } else {
            println("❌ FFmpeg failed: ${session.failStackTrace}")
        }
    }

    return outputDirName
}
