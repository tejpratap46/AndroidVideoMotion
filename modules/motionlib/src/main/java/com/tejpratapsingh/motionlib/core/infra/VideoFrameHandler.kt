package com.tejpratapsingh.motionlib.core.infra

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.ImageReader
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import com.tejpratapsingh.motionlib.core.infra.VideoFrameHandler.Companion.create
import com.tejpratapsingh.motionlib.utils.extractAudioFromVideo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.math.roundToLong

/**
 * Offscreen video frame handler backed by Media3 ExoPlayer.
 *
 * Renders into a [android.graphics.SurfaceTexture] → [android.media.ImageReader] pipeline with no UI surface,
 * making it safe to use from a [android.app.Service] or background context.
 *
 * Usage:
 * ```
 * val handler = VideoFrameHandler.create(context, uri)
 * handler.seekToFrame(42)
 * val bitmap = handler.currentFrameBitmap()
 * handler.release()
 * ```
 *
 * All public suspend functions are main-thread safe; heavy work runs on [kotlinx.coroutines.Dispatchers.IO]
 * or the dedicated player [android.os.HandlerThread].
 *
 * @param context Application or Service context (no Activity needed).
 * @param uri     URI of the video file (file://, content://, or http/s).
 */
@Suppress("ktlint:standard:backing-property-naming")
class VideoFrameHandler private constructor(
    private val context: Context,
    private val uri: Uri,
) {
    // ── State ─────────────────────────────────────────────────────────────────

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Dedicated looper for ExoPlayer — keeps it off the main thread. */
    private val playerThread = HandlerThread("VideoFrameHandler-Player").also { it.start() }
    private val playerHandler = Handler(playerThread.looper)

    private var player: ExoPlayer? = null
    private var imageReader: ImageReader? = null
    private var outputSurface: Surface? = null

    /** Video metadata resolved on [prepare]. */
    private var _durationMs: Long = 0L
    private var _fps: Float = 30f

    private var _width: Int = 1280
    private var _height: Int = 720

    val durationMs: Long get() = _durationMs
    val fps: Float get() = _fps
    val frameCount: Long get() = ((_durationMs * _fps) / 1000f).roundToLong()
    val frameDurationMs: Long get() = (1000f / _fps).roundToLong()

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        private const val SEEK_TIMEOUT_MS = 5_000L
        private const val DEFAULT_OUTPUT_WIDTH = 1280
        private const val DEFAULT_OUTPUT_HEIGHT = 720

        /**
         * Creates and prepares a [VideoFrameHandler].
         *
         * Suspends until the player is ready and metadata is resolved.
         * Must be called from a coroutine (any dispatcher).
         */
        suspend fun create(
            context: Context,
            uri: Uri,
        ): VideoFrameHandler {
            val handler = VideoFrameHandler(context.applicationContext, uri)
            handler.prepare()
            return handler
        }
    }

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Reads video metadata and wires up the offscreen ExoPlayer pipeline.
     * Called internally by [create].
     */
    @OptIn(UnstableApi::class)
    private suspend fun prepare() {
        // 1. Resolve metadata off the main thread
        withContext(Dispatchers.IO) {
            resolveMetadata()
        }

        // 2. Build ImageReader as the offscreen render target
        imageReader =
            ImageReader.newInstance(
                _width,
                _height,
                PixelFormat.RGBA_8888,
                // maxImages =
                2,
            )
        outputSurface = imageReader!!.surface

        // 3. Build ExoPlayer on its dedicated looper and wait until READY
        suspendCancellableCoroutine { cont ->
            playerHandler.post {
                val exo =
                    ExoPlayer
                        .Builder(context)
                        .setLooper(playerThread.looper)
                        .build()
                        .apply {
                            setSeekParameters(SeekParameters.EXACT)
                            setVideoSurface(outputSurface)
                            setMediaItem(MediaItem.fromUri(uri))
                            playWhenReady = false
                            prepare()
                        }

                val listener =
                    object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            if ((state == Player.STATE_READY) && cont.isActive) {
                                exo.removeListener(this)
                                player = exo
                                cont.resume(Unit)
                            }
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            if (cont.isActive) {
                                exo.removeListener(this)
                                cont.cancel(error)
                            }
                        }
                    }
                exo.addListener(listener)

                cont.invokeOnCancellation {
                    playerHandler.post {
                        try {
                            exo.removeListener(listener)
                            exo.release()
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }
            }
        }

        Timber.Forest.d("Ready — duration=${_durationMs}ms fps=$_fps frames=$frameCount size=${_width}x$_height")
    }

    /** Populates [_durationMs], [_fps], [_width], [_height] from [android.media.MediaMetadataRetriever]. */
    private fun resolveMetadata() {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)

            _durationMs = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L

            _fps = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                ?.toFloatOrNull() ?: 30f

            _width = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull()
                ?.takeIf { it > 0 } ?: DEFAULT_OUTPUT_WIDTH

            _height = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull()
                ?.takeIf { it > 0 } ?: DEFAULT_OUTPUT_HEIGHT
        } finally {
            retriever.release()
        }
    }

    // ── Seeking ───────────────────────────────────────────────────────────────

    /**
     * Seeks to an absolute position in milliseconds.
     *
     * Uses [SeekParameters.EXACT] — decodes to the precise frame.
     * Suspends until the seek has settled.
     */
    suspend fun seekToMs(positionMs: Long) {
        val target = positionMs.coerceIn(0L, _durationMs)
        awaitSeek(target)
    }

    /**
     * Seeks to a zero-based frame index.
     *
     * Frame 0 = start of video, [frameCount]-1 = last frame.
     */
    suspend fun seekToFrame(frameIndex: Long) {
        val positionMs = (frameIndex * frameDurationMs).coerceIn(0L, _durationMs)
        awaitSeek(positionMs)
    }

    /** Returns the zero-based index of the currently displayed frame. */
    @Suppress("unused")
    val currentFrameIndex: Long
        get() = ((player?.currentPosition ?: 0L) / frameDurationMs)

    /** Current playback position in milliseconds. */
    val currentPositionMs: Long
        get() = player?.currentPosition ?: 0L

    // ── Frame stepping ────────────────────────────────────────────────────────

    /**
     * Advances exactly one frame forward.
     * No-op if already at the last frame.
     */
    @Suppress("unused")
    suspend fun stepForward(frames: Int = 1) {
        val target = (currentPositionMs + (frameDurationMs * frames)).coerceAtMost(_durationMs)
        awaitSeek(target)
    }

    /**
     * Steps exactly one frame backward.
     * No-op if already at the first frame.
     */
    @Suppress("unused")
    suspend fun stepBackward(frames: Int = 1) {
        val target = (currentPositionMs - (frameDurationMs * frames)).coerceAtLeast(0L)
        awaitSeek(target)
    }

    // ── Frame capture ─────────────────────────────────────────────────────────

    /**
     * Returns a [android.graphics.Bitmap] of the frame at the current seek position.
     *
     * Uses [MediaMetadataRetriever] with [MediaMetadataRetriever.OPTION_CLOSEST]
     * for pixel-accurate extraction, independent of the render surface.
     *
     * The returned bitmap is in [android.graphics.Bitmap.Config.ARGB_8888].
     * Caller is responsible for recycling it.
     */
    suspend fun currentFrameBitmap(): Bitmap? =
        withContext(Dispatchers.IO) {
            val positionUs = currentPositionMs * 1_000L
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                retriever.getFrameAtTime(positionUs, MediaMetadataRetriever.OPTION_CLOSEST)
            } catch (e: Exception) {
                Timber.Forest.e(e, "Frame extraction failed at ${currentPositionMs}ms")
                null
            } finally {
                retriever.release()
            }
        }

    /**
     * Seeks to [positionMs] and immediately returns the decoded [Bitmap].
     * Convenience for single-call seek-and-capture flows.
     */
    @Suppress("unused")
    suspend fun frameBitmapAt(positionMs: Long): Bitmap? {
        seekToMs(positionMs)
        return currentFrameBitmap()
    }

    /**
     * Seeks to [frameIndex] and immediately returns the decoded [Bitmap].
     */
    @Suppress("unused")
    suspend fun frameBitmapAtIndex(frameIndex: Long): Bitmap? {
        seekToFrame(frameIndex)
        return currentFrameBitmap()
    }

    /**
     * Extracts the audio track from the current video to [outputFile].
     * Returns true if successful.
     */
    @Suppress("unused")
    suspend fun extractAudio(outputFile: File): Boolean =
        withContext(Dispatchers.IO) {
            extractAudioFromVideo(context, uri, outputFile)
        }

    // ── Seek helper ───────────────────────────────────────────────────────────

    /**
     * Issues a seek on the player looper and suspends until
     * the seek has completed or the timeout expires.
     */
    private suspend fun awaitSeek(positionMs: Long) {
        val exo = player ?: error("VideoFrameHandler not prepared or already released")

        val result =
            withTimeoutOrNull(SEEK_TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    playerHandler.post {
                        val listener =
                            object : Player.Listener {
                                override fun onPositionDiscontinuity(
                                    oldPosition: Player.PositionInfo,
                                    newPosition: Player.PositionInfo,
                                    reason: Int,
                                ) {
                                    if ((reason == Player.DISCONTINUITY_REASON_SEEK) && cont.isActive) {
                                        exo.removeListener(this)
                                        cont.resume(Unit)
                                    }
                                }

                                override fun onPlayerError(error: PlaybackException) {
                                    if (cont.isActive) {
                                        exo.removeListener(this)
                                        cont.cancel(error)
                                    }
                                }
                            }
                        exo.addListener(listener)
                        exo.seekTo(positionMs)

                        cont.invokeOnCancellation {
                            playerHandler.post {
                                try {
                                    exo.removeListener(listener)
                                } catch (e: Exception) {
                                    // Ignore
                                }
                            }
                        }
                    }
                }
            }

        if (result == null) {
            Timber.Forest.w("Seek to ${positionMs}ms timed out after ${SEEK_TIMEOUT_MS}ms")
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Releases all resources: ExoPlayer, ImageReader, Surface, and the player thread.
     *
     * After calling this, the instance must not be used again.
     */
    fun release() {
        playerHandler.post {
            player?.release()
            player = null
            playerThread.quitSafely()
        }
        outputSurface?.release()
        outputSurface = null
        imageReader?.close()
        imageReader = null
        scope.cancel()
        Timber.Forest.d("Released")
    }
}
