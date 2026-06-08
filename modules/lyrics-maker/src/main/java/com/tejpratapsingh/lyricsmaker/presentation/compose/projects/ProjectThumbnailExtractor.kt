package com.tejpratapsingh.lyricsmaker.presentation.compose.projects

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.lang.Exception

fun extractFirstFrame(videoPath: String): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(videoPath)
        // Request a smaller frame (e.g., 300px) and use OPTION_CLOSEST_SYNC for speed
        retriever.getScaledFrameAtTime(
            0,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
            300,
            300,
        )
    } catch (e: Exception) {
        null
    } finally {
        retriever.release()
    }
}
