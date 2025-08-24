package com.tejpratapsingh.motionlib.core.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

fun Context.loadBitmapsFromDirectory(dirName: String): List<Bitmap> {
    val dir = File(cacheDir, dirName)
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

fun Context.saveBitmapToCacheFolder(bitmap: Bitmap, subDirName: String, fileName: String) {
    val cacheSubDir = File(this.cacheDir, subDirName)
    if (!cacheSubDir.exists()) {
        cacheSubDir.mkdirs()
    }
    val file = File(cacheSubDir, fileName)
    file.outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) // Using PNG as per your pattern
    }
}