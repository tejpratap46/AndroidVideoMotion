package com.tejpratapsingh.motionlib.core

import android.view.View

interface IMotionView: OnMotionFrameListener{
}

interface OnMotionFrameListener {
    fun forFrame(frame: Int): View
}