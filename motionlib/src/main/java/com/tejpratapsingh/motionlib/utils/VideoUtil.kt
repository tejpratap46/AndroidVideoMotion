package com.tejpratapsingh.motionlib.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri

/**
 * Extracts all frames from the given video URI.
 *
 * @param context The Android context.
 * @param videoUri The URI of the video (e.g., content URI or file URI).
 * @param frameIntervalUs The interval between frames in microseconds.
 *        Use video frame rate to compute the interval (e.g., 1_000_000 / fps).
 * @return A list of bitmaps representing each extracted frame.
 */
fun extractAllVideoFrames(
    context: Context,
    videoUri: Uri,
    frameIntervalUs: Long
): List<Bitmap> {
    val retriever = MediaMetadataRetriever()
    val frames = mutableListOf<Bitmap>()

    try {
        // 1. Set data source (handles content://, file://, http(s)://, etc.)
        retriever.setDataSource(context, videoUri)

        // 2. Retrieve video duration (in microseconds)
        val durationUs = retriever.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_DURATION
        )?.toLongOrNull()?.times(1000) ?: 0L

        // 3. Iterate through timestamps from 0 to duration, stepping by frameIntervalUs
        var timeUs = 0L
        while (timeUs < durationUs) {
            // OPTIONALLY: Use OPTION_CLOSEST or OPTION_CLOSEST_SYNC
            val bitmap = retriever.getFrameAtTime(
                timeUs,
                MediaMetadataRetriever.OPTION_CLOSEST
            )
            bitmap?.let { frames.add(it) }
            timeUs += frameIntervalUs
        }
    } finally {
        retriever.release()
    }

    return frames
}

/**
 * Returns the video frame rate (FPS) extracted via MediaMetadataRetriever.
 *
 * @param context  Context for resolving the Uri.
 * @param videoUri Uri of the video (content://, file://, etc.).
 * @return FPS as a Float, or null if unavailable.
 */
fun getVideoFpsWithRetriever(context: Context, videoUri: Uri): Float? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, videoUri)
        // METADATA_KEY_CAPTURE_FRAMERATE introduced in API 24
        retriever.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE
        )?.toFloatOrNull()
    } finally {
        retriever.release()
    }
}
