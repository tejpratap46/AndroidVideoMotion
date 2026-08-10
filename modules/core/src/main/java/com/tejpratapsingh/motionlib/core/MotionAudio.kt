package com.tejpratapsingh.motionlib.core

import android.net.Uri

/**
 * Data class representing audio configuration for motion video generation.
 *
 * @property asset The [MotionAsset] (e.g. [SimpleMotionAsset]) to be used for audio.
 * @property startFrame The frame at which the audio should start from the source (trim start).
 * @property endFrame The frame at which the audio should end from the source (trim end).
 * @property delayFrame The delay in frames from the beginning of the video before the audio starts playing.
 */
data class MotionAudio(
    val asset: MotionAsset,
    val startFrame: Int, // trim start frame
    val endFrame: Int, // trim end frame
    val delayFrame: Int, // delay in frames
) {
    /**
     * For backward compatibility, the audio URI.
     */
    val audioUri: Uri get() = asset.getUri()
}
