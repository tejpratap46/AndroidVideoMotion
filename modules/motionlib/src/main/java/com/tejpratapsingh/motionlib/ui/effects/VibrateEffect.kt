package com.tejpratapsingh.motionlib.ui.effects

import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import kotlin.math.sin

class VibrateEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val amplitude: Float = 5f,
    val frequency: Float = 1f, // Hz (not really Hz here, but frequency multiplier)
) : MotionEffect {
    override lateinit var motionView: MotionView

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView
        if (frame !in startFrame..endFrame) return motionView

        val view = motionView as View

        // Using sin wave for vibration
        val offset = sin(frame.toDouble() * frequency).toFloat() * amplitude
        
        view.translationX = offset
        view.translationY = offset / 2f // Slight diagonal vibration

        return motionView
    }
}
