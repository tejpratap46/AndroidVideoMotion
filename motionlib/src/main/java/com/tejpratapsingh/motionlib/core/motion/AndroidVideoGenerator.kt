package com.tejpratapsingh.motionlib.core.motion

import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import androidx.core.graphics.scale
import com.tejpratapsingh.motionlib.core.MotionConfig
import java.io.File
import java.io.IOException

private const val MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_MPEG4 // H.264
private const val I_FRAME_INTERVAL = 5 // Keyframe interval in seconds
private const val TIMEOUT_USEC = 10000L // Timeout for MediaCodec operations

class AndroidVideoGenerator {

    companion object {
        private const val TAG = "VideoGenerator"
    }

    @Throws(IOException::class)
    fun generateVideo(
        bitmaps: List<Bitmap>,
        outputFile: File,
        motionConfig: MotionConfig
    ) {
        if (bitmaps.isEmpty()) {
            Log.w(TAG, "Bitmap list is empty. Cannot generate video.")
            return
        }

        var mediaCodec: MediaCodec? = null
        var mediaMuxer: MediaMuxer? = null
        var presentationTimeUs = 0L

        try {
            val format = MediaFormat.createVideoFormat(MIME_TYPE, motionConfig.width, motionConfig.height)
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            format.setInteger(MediaFormat.KEY_BIT_RATE, calculateBitRate(motionConfig.width, motionConfig.height, motionConfig.fps))
            format.setInteger(MediaFormat.KEY_FRAME_RATE, motionConfig.fps)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)

            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE)
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = mediaCodec.createInputSurface()
            mediaCodec.start()

            mediaMuxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var videoTrackIndex = -1
            var muxerStarted = false

            val bufferInfo = MediaCodec.BufferInfo()

            for (bitmap in bitmaps) {
                val canvas = inputSurface.lockCanvas(null)
                try {
                    val scaledBitmap = bitmap.scale(motionConfig.width, motionConfig.height)
                    canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
                    if (scaledBitmap != bitmap) {
                        scaledBitmap.recycle()
                    }
                } finally {
                    inputSurface.unlockCanvasAndPost(canvas)
                }

                while (true) {
                    val encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
                    when {
                        encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER -> break
                        encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            if (muxerStarted) throw RuntimeException("format changed after muxer start")
                            val newFormat = mediaCodec.outputFormat
                            videoTrackIndex = mediaMuxer.addTrack(newFormat)
                            mediaMuxer.start()
                            muxerStarted = true
                        }
                        encoderStatus < 0 -> Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: $encoderStatus")
                        else -> {
                            val encodedData = mediaCodec.getOutputBuffer(encoderStatus)
                                ?: throw RuntimeException("encoderOutputBuffer $encoderStatus was null")

                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                bufferInfo.size = 0
                            }

                            if (bufferInfo.size != 0) {
                                if (!muxerStarted) throw RuntimeException("muxer hasn't started")

                                encodedData.position(bufferInfo.offset)
                                encodedData.limit(bufferInfo.offset + bufferInfo.size)

                                bufferInfo.presentationTimeUs = presentationTimeUs
                                mediaMuxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                                presentationTimeUs += 1_000_000L / motionConfig.fps
                            }

                            mediaCodec.releaseOutputBuffer(encoderStatus, false)

                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                Log.d(TAG, "End of stream reached for encoder output.")
                                break
                            }
                        }
                    }
                }
            }

            mediaCodec.signalEndOfInputStream()

            drainEncoder(
                mediaCodec,
                mediaMuxer,
                bufferInfo,
                videoTrackIndex,
                muxerStarted,
                motionConfig.fps,
                presentationTimeUs
            )

            Log.i(TAG, "Video generation complete: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating video", e)
            if (outputFile.exists()) {
                outputFile.delete()
            }
            throw e
        } finally {
            try {
                mediaCodec?.stop()
                mediaCodec?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping/releasing MediaCodec", e)
            }
            try {
                mediaMuxer?.stop()
                mediaMuxer?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping/releasing MediaMuxer", e)
            }
        }
    }

    private fun drainEncoder(
        mediaCodec: MediaCodec,
        mediaMuxer: MediaMuxer?,
        bufferInfo: MediaCodec.BufferInfo,
        videoTrackIndex: Int,
        muxerStarted: Boolean,
        fps: Int,
        initialPresentationTimeUs: Long
    ) {
        var localMuxerStarted = muxerStarted
        var localVideoTrackIndex = videoTrackIndex
        var presentationTimeUs = initialPresentationTimeUs

        while (true) {
            val encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
            when {
                encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER -> break
                encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (localMuxerStarted) throw RuntimeException("format changed after muxer start (during drain)")
                    val newFormat = mediaCodec.outputFormat
                    localVideoTrackIndex = mediaMuxer!!.addTrack(newFormat)
                    mediaMuxer.start()
                    localMuxerStarted = true
                }
                encoderStatus < 0 -> Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer (during drain): $encoderStatus")
                else -> {
                    val encodedData = mediaCodec.getOutputBuffer(encoderStatus)
                        ?: throw RuntimeException("encoderOutputBuffer $encoderStatus was null (during drain)")

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        bufferInfo.size = 0
                    }

                    if (bufferInfo.size != 0) {
                        if (!localMuxerStarted) throw RuntimeException("muxer hasn't started (during drain)")

                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)

                        bufferInfo.presentationTimeUs = presentationTimeUs
                        mediaMuxer!!.writeSampleData(localVideoTrackIndex, encodedData, bufferInfo)
                        presentationTimeUs += 1_000_000L / fps
                    }

                    mediaCodec.releaseOutputBuffer(encoderStatus, false)

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Log.d(TAG, "End of stream reached for encoder output (during drain).")
                        break
                    }
                }
            }
        }
    }

    private fun calculateBitRate(width: Int, height: Int, frameRate: Int): Int {
        return (width * height * frameRate * 0.25).toInt()
    }
}
