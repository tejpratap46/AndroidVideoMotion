package com.tejpratapsingh.motionlib.media3

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import java.io.File
import kotlin.math.roundToLong

/**
 * Handles building the Media3 [Composition] from frames and audio.
 */
@UnstableApi
class Media3CompositionBuilder {

    /**
     * Builds a [Composition] from the frames in [frameDirectory] and the provided [motionAudio].
     */
    fun build(
        frameDirectory: File,
        motionAudio: List<MotionAudio>,
        motionConfig: MotionConfig,
    ): Composition {
        val frameDurationMs = (1_000.0 / motionConfig.fps).roundToLong().coerceAtLeast(1L)
        val videoItems =
            frameDirectory
                .listFiles { file -> file.extension.equals("png", ignoreCase = true) }
                ?.asSequence()
                ?.sortedBy { it.name }
                ?.map { frameFile ->
                    val imageMediaItem =
                        MediaItem
                            .Builder()
                            .setUri(Uri.fromFile(frameFile))
                            .setImageDurationMs(frameDurationMs)
                            .build()

                    EditedMediaItem
                        .Builder(imageMediaItem)
                        .setFrameRate(motionConfig.fps)
                        .setRemoveAudio(true)
                        .build()
                }?.toList()
                .orEmpty()

        require(videoItems.isNotEmpty()) { "No frames were generated for Media3 Transformer export." }

        val sequences = mutableListOf(EditedMediaItemSequence.withVideoFrom(videoItems))
        motionAudio.mapTo(sequences) { audio -> buildAudioSequence(audio, motionConfig.fps) }

        return Composition
            .Builder(sequences)
            .setTransmuxAudio(false)
            .setTransmuxVideo(false)
            .build()
    }

    private fun buildAudioSequence(
        audio: MotionAudio,
        fps: Int,
    ): EditedMediaItemSequence {
        val startMs = frameToMs(audio.startFrame, fps)
        val endMs = frameToMs(audio.endFrame, fps)
        val mediaItem =
            MediaItem
                .Builder()
                .setUri(Uri.fromFile(audio.file))
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration
                        .Builder()
                        .setStartPositionMs(startMs)
                        .setEndPositionMs(endMs.takeIf { it > startMs } ?: C.TIME_END_OF_SOURCE)
                        .build(),
                ).build()

        val audioItem =
            EditedMediaItem
                .Builder(mediaItem)
                .setRemoveVideo(true)
                .build()

        return if (audio.delayFrame > 0) {
            EditedMediaItemSequence
                .Builder(setOf(C.TRACK_TYPE_AUDIO))
                .addGap(frameToUs(audio.delayFrame, fps))
                .addItem(audioItem)
                .build()
        } else {
            EditedMediaItemSequence.withAudioFrom(listOf(audioItem))
        }
    }

    private fun frameToMs(frame: Int, fps: Int): Long = 
        ((frame.toDouble() / fps.toDouble()) * 1_000).roundToLong()

    private fun frameToUs(frame: Int, fps: Int): Long = 
        ((frame.toDouble() / fps.toDouble()) * 1_000_000).roundToLong()
}
