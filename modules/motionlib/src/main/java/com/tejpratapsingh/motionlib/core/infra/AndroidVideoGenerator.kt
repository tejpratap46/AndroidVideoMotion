package com.tejpratapsingh.motionlib.core.infra

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import timber.log.Timber
import androidx.core.graphics.scale
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

class AndroidVideoGenerator {
    companion object {
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
        motionAudio: List<MotionAudio> = emptyList(),
    ) {
        val frameCount = getBitmapCount(bitmaps, inputDir)
        if (frameCount == 0) {
            Timber.w("No bitmaps provided or found. Cannot generate video.")
            return
        }

        val motionConfig: MotionConfig = provideCurrentConfig()
        Timber.i("generateVideo: Starting production of $frameCount frames at ${motionConfig.fps} fps")
        Timber.d("generateVideo: Output file: ${outputFile.absolutePath}")

        var mediaCodec: MediaCodec? = null
        var mediaMuxer: MediaMuxer? = null
        var presentationTimeUs = 0L
        var muxerStarted = false

        try {
            val format =
                MediaFormat.createVideoFormat(MIME_TYPE, motionConfig.aspectRatio.width, motionConfig.aspectRatio.height)
            format.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
            )
            format.setInteger(
                MediaFormat.KEY_BIT_RATE,
                calculateBitRate(motionConfig.aspectRatio.width, motionConfig.aspectRatio.height, motionConfig.fps),
            )
            format.setInteger(MediaFormat.KEY_FRAME_RATE, motionConfig.fps)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)

            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE)
            Timber.d("generateVideo: Created encoder for $MIME_TYPE")
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = mediaCodec.createInputSurface()
            mediaCodec.start()
            Timber.d("generateVideo: Encoder started")

            // Collect audio track formats beforehand (use original formats; no re-encode here)
            val audioTrackFormats = mutableListOf<MediaFormat>()
            for (audio in motionAudio) {
                Timber.d("generateVideo: Extracting audio format from ${audio.file.name}")
                val extractor = MediaExtractor()
                extractor.setDataSource(audio.file.absolutePath)
                for (i in 0 until extractor.trackCount) {
                    val fmt = extractor.getTrackFormat(i)
                    val mime = fmt.getString(MediaFormat.KEY_MIME) ?: ""
                    if (mime.startsWith("audio/")) {
                        audioTrackFormats.add(fmt)
                        break
                    }
                }
                extractor.release()
            }

            mediaMuxer =
                MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            Timber.d("generateVideo: MediaMuxer initialized")
            var videoTrackIndex = -1
            val muxerAudioTrackIndices = mutableListOf<Int>()

            val bufferInfo = MediaCodec.BufferInfo()

            // Encode frames
            for (i in 0 until frameCount) {
                if (i % 10 == 0) {
                    Timber.v("generateVideo: Encoding frame $i/$frameCount")
                }
                val canvas = inputSurface.lockCanvas(null)
                val bitmap = getBitmap(bitmaps, inputDir, i) ?: continue

                try {
                    val scaledBitmap = bitmap.scale(motionConfig.aspectRatio.width, motionConfig.aspectRatio.height)
                    canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
                    if (scaledBitmap != bitmap) {
                        scaledBitmap.recycle()
                    }
                } finally {
                    inputSurface.unlockCanvasAndPost(canvas)
                }

                // Drain available encoder output so we can catch INFO_OUTPUT_FORMAT_CHANGED early
                while (true) {
                    val encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
                    when {
                        encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                            break
                        }

                        encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            if (muxerStarted) throw RuntimeException("format changed after muxer start")
                            val newFormat = mediaCodec.outputFormat
                            Timber.d("generateVideo: Video output format changed: $newFormat")
                            videoTrackIndex = mediaMuxer.addTrack(newFormat)

                            // Add audio tracks BEFORE starting the muxer
                            for (fmt in audioTrackFormats) {
                                Timber.d("generateVideo: Adding audio track to muxer: $fmt")
                                muxerAudioTrackIndices.add(mediaMuxer.addTrack(fmt))
                            }

                            mediaMuxer.start()
                            muxerStarted = true
                            Timber.d("generateVideo: MediaMuxer started")
                        }

                        encoderStatus < 0 -> {
                            Timber.w("unexpected result from encoder.dequeueOutputBuffer: $encoderStatus")
                        }

                        else -> {
                            val encodedData =
                                mediaCodec.getOutputBuffer(encoderStatus) ?: throw RuntimeException(
                                    "encoderOutputBuffer $encoderStatus was null",
                                )

                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                // Codec config data; ignore
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
                                Timber.d("End of stream reached for encoder output.")
                                break
                            }
                        }
                    }
                }
            }

            // Signal end of input and make sure we drain until EOS
            mediaCodec.signalEndOfInputStream()

            drainEncoder(
                mediaCodec = mediaCodec,
                mediaMuxer = mediaMuxer,
                bufferInfo = bufferInfo,
                videoTrackIndex = videoTrackIndex,
                muxerStarted = muxerStarted,
                fps = motionConfig.fps,
                initialPresentationTimeUs = presentationTimeUs,
                audioTrackFormats = audioTrackFormats,
                muxerAudioTrackIndices = muxerAudioTrackIndices,
            )

            // Now copy audio samples into the muxer (timeline aligned by frames -> microseconds)
            if (motionAudio.isNotEmpty() && muxerAudioTrackIndices.isNotEmpty()) {
                Timber.d("generateVideo: adding audio")
                muxAudioTracks(mediaMuxer, motionAudio, motionConfig.fps, muxerAudioTrackIndices)
            }

            Timber.i("Video generation complete: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            Timber.e(e, "Error generating video")
            if (outputFile.exists()) {
                outputFile.delete()
            }
            throw e
        } finally {
            try {
                mediaCodec?.stop()
                mediaCodec?.release()
            } catch (e: Exception) {
                Timber.e(e, "Error stopping/releasing MediaCodec")
            }
            try {
                if (muxerStarted) {
                    mediaMuxer?.stop()
                }
                mediaMuxer?.release()
            } catch (e: Exception) {
                Timber.e(e, "Error stopping/releasing MediaMuxer")
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun muxAudioTracks(
        mediaMuxer: MediaMuxer,
        audioSources: List<MotionAudio>,
        fps: Int,
        audioTrackIndices: List<Int>,
    ) {
        Timber.d("muxAudioTracks: adding audio")
        val bufferSize = 1 * 1024 * 1024
        val buffer = ByteBuffer.allocate(bufferSize)
        val bufferInfo = MediaCodec.BufferInfo()

        for ((sourceIndex, audio) in audioSources.withIndex()) {
            val extractor = MediaExtractor()
            extractor.setDataSource(audio.file.absolutePath)

            var audioTrackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    extractor.selectTrack(i)
                    audioTrackIndex = i
                    break
                }
            }

            if (audioTrackIndex < 0) {
                extractor.release()
                continue
            }

            val muxerAudioTrackIndex = audioTrackIndices[sourceIndex]

            val startUs = audio.startFrame * 1_000_000L / fps
            val endUs = audio.endFrame * 1_000_000L / fps
            val insertOffsetUs = audio.delayFrame * 1_000_000L / fps

            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) break

                val sampleTimeUs = extractor.sampleTime
                if (sampleTimeUs > endUs) break

                // Keep original extractor flags; they are already in muxer-friendly form
                bufferInfo.flags = extractor.sampleFlags

                // Shift timeline into final video timeline window
                bufferInfo.presentationTimeUs = (sampleTimeUs - startUs) + insertOffsetUs

                mediaMuxer.writeSampleData(muxerAudioTrackIndex, buffer, bufferInfo)
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
        initialPresentationTimeUs: Long,
        audioTrackFormats: List<MediaFormat>,
        muxerAudioTrackIndices: MutableList<Int>,
    ) {
        var localMuxerStarted = muxerStarted
        var localVideoTrackIndex = videoTrackIndex
        var presentationTimeUs = initialPresentationTimeUs

        while (true) {
            val encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
            when {
                encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    break
                }

                encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    // This can happen if no encoded output was dequeued earlier.
                    if (localMuxerStarted) throw RuntimeException("format changed after muxer start (during drain)")
                    val newFormat = mediaCodec.outputFormat
                    localVideoTrackIndex = mediaMuxer!!.addTrack(newFormat)

                    // IMPORTANT: add audio tracks BEFORE starting the muxer
                    for (fmt in audioTrackFormats) {
                        muxerAudioTrackIndices.add(mediaMuxer.addTrack(fmt))
                    }

                    mediaMuxer.start()
                    localMuxerStarted = true
                }

                encoderStatus < 0 -> {
                    Timber.w("unexpected result from encoder.dequeueOutputBuffer (during drain): $encoderStatus")
                }

                else -> {
                    val encodedData =
                        mediaCodec.getOutputBuffer(encoderStatus)
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
                        Timber.d("End of stream reached for encoder output (during drain).")
                        break
                    }
                }
            }
        }
    }

    private fun calculateBitRate(
        width: Int,
        height: Int,
        frameRate: Int,
    ): Int = (width * height * frameRate * 0.25).toInt()

    private fun getBitmapCount(
        bitmaps: List<Bitmap> = mutableListOf(),
        inputDir: File? = null,
    ): Int =
        if (inputDir != null) {
            initBitmapFiles(inputDir)
            bitmapFiles?.size ?: 0
        } else {
            bitmaps.size
        }

    private fun getBitmap(
        bitmaps: List<Bitmap> = mutableListOf(),
        inputDir: File? = null,
        index: Int,
    ): Bitmap? =
        if (inputDir != null) {
            initBitmapFiles(inputDir)

            bitmapFiles?.getOrNull(index)?.let { file ->
                BitmapFactory.decodeFile(file.absolutePath)
            }
        } else {
            bitmaps.getOrNull(index)
        }

    private fun initBitmapFiles(inputDir: File) {
        if (bitmapFiles == null) {
            bitmapFiles =
                inputDir
                    .listFiles { file ->
                        file.extension.lowercase() in listOf("png", "jpg", "jpeg", "webp")
                    }?.sortedBy { file ->
                        file.nameWithoutExtension.filter { it.isDigit() }.toIntOrNull() ?: 0
                    }
        }
    }
}
