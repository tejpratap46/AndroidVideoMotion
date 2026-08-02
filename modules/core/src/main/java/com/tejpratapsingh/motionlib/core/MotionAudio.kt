package com.tejpratapsingh.motionlib.core

import android.net.Uri

/**
 * Data class representing audio configuration for motion video generation.
 *
 * @property audioUri The audio URI to be used (file://, content://, http://, etc.).
 * @property startFrame The frame at which the audio should start (trim start).
 * @property endFrame The frame at which the audio should end (trim end).
 * @property delayFrame The delay in frames before the audio starts playing.
 */
data class MotionAudio(
    val audioUri: Uri,
    val startFrame: Int, // trim start frame
    val endFrame: Int, // trim end frame
    val delayFrame: Int, // delay in frames
)
