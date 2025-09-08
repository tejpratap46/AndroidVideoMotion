package com.tejpratapsingh.motionlib.core

import android.graphics.Bitmap

interface MotionView : OnMotionFrameListener {
    val startFrame: Int
    val endFrame: Int

    val loop: Pair<Int, Int>

    fun getViewBitmap(): Bitmap
}

fun interface OnMotionFrameListener {
    fun forFrame(frame: Int): MotionView
}