package com.tejpratapsingh.motionlib.core

import android.graphics.Bitmap

interface IMotionView : OnMotionFrameListener {
    val startFrame: Int
    val endFrame: Int
    var motionConfig: MotionConfig

    fun getViewBitmap(): Bitmap
}

interface OnMotionFrameListener {
    fun forFrame(frame: Int): IMotionView
}