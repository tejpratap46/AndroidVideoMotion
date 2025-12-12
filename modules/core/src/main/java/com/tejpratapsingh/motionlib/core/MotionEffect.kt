package com.tejpratapsingh.motionlib.core

interface MotionEffect : OnMotionFrameListener {
    val motionView: MotionView
    val startFrame: Int
    val endFrame: Int
}
