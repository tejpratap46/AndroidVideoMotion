package com.tejpratapsingh.motionlib.core

import android.graphics.Bitmap

interface MotionView : OnMotionFrameListener {
    val startFrame: Int
    val endFrame: Int

    val loop: Pair<Int, Int>

    val effects: List<MotionEffect>

    val layoutInfo: MotionLayoutInfo
        get() = MotionLayoutInfo()

    fun addEffect(effect: MotionEffect)

    fun getViewBitmap(): Bitmap
}
