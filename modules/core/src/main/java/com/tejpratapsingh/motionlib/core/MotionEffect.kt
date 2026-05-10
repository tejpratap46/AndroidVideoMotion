package com.tejpratapsingh.motionlib.core

interface MotionEffect : OnMotionFrameListener {
    var motionView: MotionView
    val startFrame: Int
    val endFrame: Int
}
