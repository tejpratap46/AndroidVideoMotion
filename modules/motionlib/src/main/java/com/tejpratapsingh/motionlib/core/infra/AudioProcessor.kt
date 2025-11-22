package com.tejpratapsingh.motionlib.core.infra

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File

fun extractWaveformFromFile(file: File): List<Float> {
    val extractor = MediaExtractor()
    extractor.setDataSource(file.absolutePath)

    // Find first audio track
    var trackIndex = -1
    for (i in 0 until extractor.trackCount) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME)
        if (mime?.startsWith("audio/") == true) {
            trackIndex = i
            extractor.selectTrack(i)
            break
        }
    }

    if (trackIndex == -1) return emptyList()

    val format = extractor.getTrackFormat(trackIndex)
    val mime = format.getString(MediaFormat.KEY_MIME) ?: return emptyList()

    val codec = MediaCodec.createDecoderByType(mime)
    codec.configure(format, null, null, 0)
    codec.start()

    val bufferInfo = MediaCodec.BufferInfo()
    val amplitudes = mutableListOf<Float>()

    var isEOS = false
    while (!isEOS) {
        val inputIndex = codec.dequeueInputBuffer(10_000)
        if (inputIndex >= 0) {
            val inputBuffer = codec.getInputBuffer(inputIndex)!!
            val sampleSize = extractor.readSampleData(inputBuffer, 0)

            if (sampleSize < 0) {
                codec.queueInputBuffer(
                    inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
                isEOS = true
            } else {
                codec.queueInputBuffer(
                    inputIndex, 0, sampleSize, extractor.sampleTime, 0
                )
                extractor.advance()
            }
        }

        val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)
        if (outputIndex >= 0) {
            val outputBuffer = codec.getOutputBuffer(outputIndex)!!
            val pcm = ShortArray(bufferInfo.size / 2)
            outputBuffer.asShortBuffer().get(pcm)

            // Convert PCM → amplitude normalized between -1f..1f
            pcm.forEach { sample ->
                amplitudes.add(sample.toFloat() / Short.MAX_VALUE)
            }

            codec.releaseOutputBuffer(outputIndex, false)
        }
    }

    codec.stop()
    codec.release()
    extractor.release()

    return amplitudes
}
