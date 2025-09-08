package com.tejpratapsingh.motionlib.ui.custom.audio

import android.content.Context
import android.graphics.Paint
import com.tejpratapsingh.motionlib.core.motion.BaseMotionView

open class BaseAudioWaveformView(context: Context,
                                 override var startFrame: Int,
                                 override var endFrame: Int
) : BaseMotionView(context, startFrame, endFrame) {
    protected var currentFrame: Int = 0

    protected val paint = Paint().apply {
        color = 0xFF009688.toInt()   // Teal spikes
        strokeWidth = 3f
        isAntiAlias = true
    }

    protected val cursorPaint = Paint().apply {
        color = 0xFFFF5722.toInt()   // Orange playback cursor
        strokeWidth = 5f
        isAntiAlias = true
    }
}