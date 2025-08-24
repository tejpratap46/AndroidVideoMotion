package com.tejpratapsingh.motionlib.core

import java.io.File

data class MotionAudio(
    val file: File,
    val startFrame: Int = 0,             // frame where audio should start in its file
    val endFrame: Int = Int.MAX_VALUE,   // last frame to take from audio
    val insertAtFrame: Int = 0           // position in video (frame) to place the audio
)