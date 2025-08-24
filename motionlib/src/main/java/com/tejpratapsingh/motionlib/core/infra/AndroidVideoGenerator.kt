package com.tejpratapsingh.motionlib.core.infra

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import androidx.core.graphics.scale
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

class AndroidVideoGenerator {

    companion object {
        private const val TAG = "VideoGenerator"

        private const val MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_MPEG4 // H.264
        private const val I_FRAME_INTERVAL = 5 // Keyframe interval in seconds
        private const val TIMEOUT_USEC = 10000L // Timeout for MediaCodec operations
    }

    private var bitmapFiles: List<File>? = null

    /**
     * Generates a video from a list of bitmaps or a directory of image files.
     *
     * The function encodes the provided images into a video stream using MediaCodec
     * and writes the output to the specified file using MediaMuxer. It also
     * allows for adding multiple audio tracks to the video.
     *
     * At least one source of bitmaps (either `bitmaps` list or `inputDir`) must be provided.
     * If both are provided, `inputDir` takes precedence if it's not null and contains valid images.
     * If neither is provided, the function will log a warning and return without generating a video.
     *
     * The output video will be in MP4 format with H.264 video encoding.
     *
     * @param bitmaps A list of [Bitmap] objects to be included in the video.
     *                Defaults to an empty list. If `inputDir` is also provided and valid,
     *                this parameter will be ignored.
     * @param inputDir A [File] object representing the directory containing image files
     *                 (PNG, JPG, JPEG, WEBP). Images will be sorted numerically by their filenames.
     *                 Defaults to null. If provided, this will be used as the source of frames.
     * @param outputFile The [File] object where the generated video will be saved.
     *                   If an error occurs during generation and this file exists, it will be deleted.
     * @param motionConfig A [MotionConfig] object specifying video properties like
     *                     width, height, and frames per second (fps).
     * @param motionAudios A list of [MotionAudio] objects to be mixed into the video.
     *                     Each [AudioSource] defines the audio file, start/end times for trimming,
     *                     and the insertion point in the final video. Defaults to an empty list.
     * @throws IOException If there is an error during file I/O operations (e.g., creating the output file).
     * @throws RuntimeException If there is an unexpected error during the MediaCodec or MediaMuxer
     *                          operations (e.g., format changes after muxer start, null buffers).
     * @throws Exception For any other unhandled exceptions during the video generation process.
     */
    @Throws(IOException::class)
    fun generateVideo(
        bitmaps: List<Bitmap> = emptyList(),
        inputDir: File? = null,
        outputFile: File,
        motionConfig: MotionConfig,
        motionAudios: List<MotionAudio> = emptyList()
    ) {
        if (bitmaps.isEmpty() && inputDir == null) {
            Log.w(TAG, "No bitmaps provided. Cannot generate video.")
            return
        }

        var mediaCodec: MediaCodec? = null
        var mediaMuxer: MediaMuxer? = null
        var presentationTimeUs = 0L

        try {
            val format =
                MediaFormat.createVideoFormat(MIME_TYPE, motionConfig.width, motionConfig.height)
            format.setInteger(
                MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            format.setInteger(
                MediaFormat.KEY_BIT_RATE,
                calculateBitRate(motionConfig.width, motionConfig.height, motionConfig.fps)
            )
            format.setInteger(MediaFormat.KEY_FRAME_RATE, motionConfig.fps)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)

            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE)
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = mediaCodec.createInputSurface()
            mediaCodec.start()

            mediaMuxer =
                MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var videoTrackIndex = -1
            var muxerStarted = false

            val bufferInfo = MediaCodec.BufferInfo()

            for (i in 0 until getBitmapCount(bitmaps, inputDir)) {
                val canvas = inputSurface.lockCanvas(null)
                val bitmap = getBitmap(bitmaps, inputDir, i) ?: continue

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

                        encoderStatus < 0 -> Log.w(
                            TAG,
                            "unexpected result from encoder.dequeueOutputBuffer: $encoderStatus"
                        )

                        else -> {
                            val encodedData =
                                mediaCodec.getOutputBuffer(encoderStatus) ?: throw RuntimeException(
                                    "encoderOutputBuffer $encoderStatus was null"
                                )

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
                mediaCodec = mediaCodec,
                mediaMuxer = mediaMuxer,
                bufferInfo = bufferInfo,
                videoTrackIndex = videoTrackIndex,
                muxerStarted = muxerStarted,
                fps = motionConfig.fps,
                initialPresentationTimeUs = presentationTimeUs
            )

            // Add audio sources if any
            if (motionAudios.isNotEmpty()) {
                muxAudioTracks(mediaMuxer, motionAudios, motionConfig.fps)
            }

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

    /**
     * Muxes multiple audio tracks into the video using the provided MediaMuxer.
     *
     * This function iterates through a list of [MotionAudio] objects, each representing
     * an audio source to be included in the video. For each audio source, it:
     * 1. Initializes a [MediaExtractor] to read the audio data from the specified file.
     * 2. Finds the first audio track in the source file.
     * 3. Adds this audio track to the [MediaMuxer].
     * 4. Calculates the start, end, and insertion timestamps in microseconds based on
     *    the frame numbers provided in the [MotionAudio] object and the video's FPS.
     * 5. Seeks the extractor to the calculated start time.
     * 6. Reads audio samples from the extractor, adjusts their presentation timestamps
     *    according to the trimming and insertion points, and writes them to the muxer.
     *    This process continues until the end of the specified segment or the end of the
     *    audio stream is reached.
     * 7. Releases the [MediaExtractor] for the current audio source.
     *
     * If an audio source file does not contain an audio track, it is skipped.
     *
     * @param mediaMuxer The [MediaMuxer] instance to which the audio tracks will be added.
     *                   This muxer should already have the video track added and be started
     *                   if video encoding has begun.
     * @param audioSources A list of [MotionAudio] objects, each defining an audio file
     *                     and the timing for its inclusion in the final video.
     * @param fps The frames per second of the video, used to convert frame-based timings
     *            in [MotionAudio] to microsecond-based timestamps for audio processing.
     */
    private fun muxAudioTracks(mediaMuxer: MediaMuxer?, audioSources: List<MotionAudio>, fps: Int) {
        val bufferSize = 1 * 1024 * 1024
        val buffer = ByteBuffer.allocate(bufferSize)
        val bufferInfo = MediaCodec.BufferInfo()

        for (audio in audioSources) {
            val extractor = MediaExtractor()
            extractor.setDataSource(audio.file.absolutePath)

            var audioTrackIndex = -1
            var muxerAudioTrackIndex = -1

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    extractor.selectTrack(i)
                    muxerAudioTrackIndex = mediaMuxer!!.addTrack(format)
                    audioTrackIndex = i
                    break
                }
            }

            if (audioTrackIndex < 0 || muxerAudioTrackIndex < 0) {
                extractor.release()
                continue
            }

            // Convert frames → microseconds
            val startUs = audio.startFrame * 1_000_000L / fps
            val endUs = audio.endFrame * 1_000_000L / fps
            val insertOffsetUs = audio.insertAtFrame * 1_000_000L / fps

            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) break

                val sampleTimeUs = extractor.sampleTime
                if (sampleTimeUs > endUs) break

                bufferInfo.presentationTimeUs = (sampleTimeUs - startUs) + insertOffsetUs

                // Map extractor flags → codec flags
                val sampleFlags = extractor.sampleFlags
                var codecFlags = 0
                if (sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) {
                    codecFlags = codecFlags or MediaCodec.BUFFER_FLAG_KEY_FRAME
                }
                if (sampleFlags and MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME != 0) {
                    codecFlags = codecFlags or MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
                }
                bufferInfo.flags = codecFlags

                mediaMuxer!!.writeSampleData(muxerAudioTrackIndex, buffer, bufferInfo)
                extractor.advance()
            }

            extractor.release()
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

                encoderStatus < 0 -> Log.w(
                    TAG,
                    "unexpected result from encoder.dequeueOutputBuffer (during drain): $encoderStatus"
                )

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

    private fun getBitmapCount(
        bitmaps: List<Bitmap> = mutableListOf(), inputDir: File? = null
    ): Int = if (inputDir != null) {
        initBitmapFiles(inputDir)
        bitmapFiles?.size ?: 0
    } else {
        bitmaps.size
    }

    private fun getBitmap(
        bitmaps: List<Bitmap> = mutableListOf(), inputDir: File? = null, index: Int
    ): Bitmap? = if (inputDir != null) {
        initBitmapFiles(inputDir)

        bitmapFiles?.getOrNull(index)?.let { file ->
            BitmapFactory.decodeFile(file.absolutePath)
        }
    } else {
        bitmaps.getOrNull(index)
    }

    private fun initBitmapFiles(inputDir: File) {
        if (bitmapFiles == null) {
            bitmapFiles = inputDir.listFiles { file ->
                file.extension.lowercase() in listOf("png", "jpg", "jpeg", "webp")
            }?.sortedBy { file ->
                file.nameWithoutExtension.filter { it.isDigit() }.toIntOrNull() ?: 0
            }
        }
    }
}
