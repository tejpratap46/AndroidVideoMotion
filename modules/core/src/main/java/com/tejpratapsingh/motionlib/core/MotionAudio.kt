package com.tejpratapsingh.motionlib.core

import java.io.File

/**
 * Data class representing audio configuration for motion video generation.
 *
 * @property file The audio file to be used.
 * @property startFrame The frame at which the audio should start (trim start).
 * @property endFrame The frame at which the audio should end (trim end).
 * @property delayFrame The delay in frames before the audio starts playing.
 */
data class MotionAudio(
    val file: File,
    val startFrame: Int,   // trim start frame
    val endFrame: Int,     // trim end frame
    val delayFrame: Int    // delay in frames
)