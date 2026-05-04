package com.tejpratapsingh.motionlib.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer

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
    frameIntervalUs: Long,
): List<Bitmap> {
    val retriever = MediaMetadataRetriever()
    val frames = mutableListOf<Bitmap>()

    try {
        // 1. Set data source (handles content://, file://, http(s)://, etc.)
        retriever.setDataSource(context, videoUri)

        // 2. Retrieve video duration (in microseconds)
        val durationUs =
            retriever
                .extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION,
                )?.toLongOrNull()
                ?.times(1000) ?: 0L

        // 3. Iterate through timestamps from 0 to duration, stepping by frameIntervalUs
        var timeUs = 0L
        while (timeUs < durationUs) {
            // OPTIONALLY: Use OPTION_CLOSEST or OPTION_CLOSEST_SYNC
            val bitmap =
                retriever.getFrameAtTime(
                    timeUs,
                    MediaMetadataRetriever.OPTION_CLOSEST,
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
fun getVideoFpsWithRetriever(
    context: Context,
    videoUri: Uri,
): Float? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, videoUri)
        // METADATA_KEY_CAPTURE_FRAMERATE introduced in API 24
        retriever
            .extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE,
            )?.toFloatOrNull()
    } finally {
        retriever.release()
    }
}

/**
 * Extracts the audio track from the given video source and saves it to [outputFile].
 *
 * @param context The Android context.
 * @param videoUri The URI of the video source.
 * @param outputFile The file to save the extracted audio to (e.g., .m4a).
 * @return True if extraction was successful.
 */
fun extractAudioFromVideo(
    context: Context,
    videoUri: Uri,
    outputFile: File,
): Boolean {
    val extractor = MediaExtractor()
    var muxer: MediaMuxer? = null
    try {
        extractor.setDataSource(context, videoUri, null)

        // 1. Find the audio track
        var audioTrackIndex = -1
        var format: MediaFormat? = null
        for (i in 0 until extractor.trackCount) {
            val trackFormat = extractor.getTrackFormat(i)
            val mime = trackFormat.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                audioTrackIndex = i
                format = trackFormat
                break
            }
        }

        if ((audioTrackIndex == -1) || (format == null)) {
            Timber.e("No audio track found in $videoUri")
            return false
        }

        // 2. Setup Muxer
        extractor.selectTrack(audioTrackIndex)
        muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val muxerTrackIndex = muxer.addTrack(format)
        muxer.start()

        // 3. Copy samples
        val bufferSize = 1024 * 1024 // 1MB buffer
        val byteBuffer = ByteBuffer.allocate(bufferSize)
        val bufferInfo = MediaCodec.BufferInfo()

        while (true) {
            bufferInfo.offset = 0
            bufferInfo.size = extractor.readSampleData(byteBuffer, 0)
            if (bufferInfo.size < 0) {
                break
            }
            bufferInfo.presentationTimeUs = extractor.sampleTime
            @Suppress("WrongConstant")
            bufferInfo.flags = extractor.sampleFlags
            muxer.writeSampleData(muxerTrackIndex, byteBuffer, bufferInfo)
            extractor.advance()
        }

        return true
    } catch (e: Exception) {
        Timber.e(e, "Error extracting audio from $videoUri")
        return false
    } finally {
        extractor.release()
        try {
            muxer?.stop()
            muxer?.release()
        } catch (_: Exception) {
            // Ignore stop/release errors if muxer was never started
        }
    }
}
