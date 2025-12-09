package com.tejpratapsingh.motionlib.core

import android.graphics.Bitmap

interface MotionPlugin {
    fun apply(input: Bitmap): Bitmap
}