package com.tejpratapsingh.motionlib.core

interface IMotionView: OnMotionFrameListener{
}

interface OnMotionFrameListener {
    fun forFrame(frame: Int)
}