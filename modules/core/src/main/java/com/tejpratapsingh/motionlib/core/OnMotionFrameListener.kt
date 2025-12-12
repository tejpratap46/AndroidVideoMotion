package com.tejpratapsingh.motionlib.core

fun interface OnMotionFrameListener {
    fun forFrame(frame: Int): MotionView
}
