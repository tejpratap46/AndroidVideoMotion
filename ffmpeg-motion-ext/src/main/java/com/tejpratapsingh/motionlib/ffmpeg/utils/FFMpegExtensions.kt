package com.tejpratapsingh.motionlib.ffmpeg.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import java.security.MessageDigest

fun extractFramesFromVideo(
    context: Context, videoFile: File, outputDirName: String = "frames"
): String {
    val outputDir = File(context.cacheDir, outputDirName)
    if (!outputDir.exists()) outputDir.mkdirs()

    // Output path pattern for frames: frame_0001.png, frame_0002.png, ...
    val outputPattern = File(outputDir, "frame_%03d.png").absolutePath

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

fun loadBitmapsFromDirectory(context: Context, dirName: String): List<Bitmap> {
    val dir = File(context.cacheDir, dirName)
    if (!dir.exists() || !dir.isDirectory) {
        return emptyList()
    }

    return dir.listFiles { file ->
        file.extension.lowercase() in listOf("png", "jpg", "jpeg", "webp")
    }?.sortedBy { file ->
        // Extract digits from filename, default to 0 if no digits found
        file.nameWithoutExtension.filter { it.isDigit() }.toIntOrNull() ?: 0
    }?.mapNotNull { file ->
        BitmapFactory.decodeFile(file.absolutePath)
    } ?: emptyList()
}

fun String.md5(): String {
    val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())

    return bytes.joinToString("") { "%02x".format(it) }
}