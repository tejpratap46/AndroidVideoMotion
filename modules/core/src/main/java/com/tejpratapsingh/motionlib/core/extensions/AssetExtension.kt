package com.tejpratapsingh.motionlib.core.extensions

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * Utility function to get a URI from an asset file path.
 * This function copies the asset file to the app's cache directory and returns a URI for it.
 *
 * @param assetFilePath The path of the asset file relative to the assets directory.
 * @return A URI pointing to the copied asset file in the cache directory.
 */
fun Context.getUriFromAsset(assetFilePath: String): Uri {
    // Create the output file with nested folders
    val outFile = File(cacheDir, assetFilePath)
    outFile.parentFile?.mkdirs() // Create parent directories if needed

    // Copy from assets to destination
    assets.open(assetFilePath).use { inputStream ->
        FileOutputStream(outFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    return Uri.fromFile(outFile) // For internal use only
}

fun Context.getFileFromAsset(assetFilePath: String): File {
    // Create the output file with nested folders
    val outFile = File(cacheDir, assetFilePath)
    outFile.parentFile?.mkdirs() // Create parent directories if needed

    // Copy from assets to destination
    assets.open(assetFilePath).use { inputStream ->
        FileOutputStream(outFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    return outFile // For internal use only
}
