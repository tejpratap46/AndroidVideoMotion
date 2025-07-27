package com.tejpratapsingh.motionlib.ffmpeg

import android.content.Context
import android.graphics.Bitmap
import java.io.File

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
