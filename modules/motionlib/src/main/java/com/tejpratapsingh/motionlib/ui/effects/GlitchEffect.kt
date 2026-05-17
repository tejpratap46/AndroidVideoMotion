package com.tejpratapsingh.motionlib.ui.effects

import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import java.util.Random

class GlitchEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val intensity: Float = 10f,
) : MotionEffect {
    override lateinit var motionView: MotionView
    private val random = Random()

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView
        if (frame !in startFrame..endFrame) return motionView

        val view = motionView as View

        // Jitter translation
        view.translationX = (random.nextFloat() * 2 - 1) * intensity
        view.translationY = (random.nextFloat() * 2 - 1) * intensity

        // Occasional alpha drop
        if (random.nextFloat() > 0.8f) {
            view.alpha = random.nextFloat() * 0.5f + 0.5f
        } else {
            view.alpha = 1f
        }

        // Occasional scale jitter
        if (random.nextFloat() > 0.9f) {
            view.scaleX = 1f + (random.nextFloat() * 2 - 1) * 0.1f
            view.scaleY = 1f + (random.nextFloat() * 2 - 1) * 0.1f
        } else {
            view.scaleX = 1f
            view.scaleY = 1f
        }

        return motionView
    }
}
