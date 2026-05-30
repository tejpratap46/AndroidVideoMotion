package com.tejpratapsingh.motionlib.ui.effects

import android.graphics.RenderEffect
import android.os.Build
import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView

/**
 * A [MotionEffect] that chains two other [MotionEffect]s together using [RenderEffect.createChainEffect].
 * Note: This implementation is a placeholder to show how chaining could work.
 * In the current architecture, effects apply themselves directly to the view.
 */
class ChainEffect(
    override val startFrame: Int,
    override val endFrame: Int,
    val outerEffect: MotionEffect,
    val innerEffect: MotionEffect,
) : MotionEffect {
    private var _motionView: MotionView? = null
    override var motionView: MotionView
        get() = _motionView!!
        set(value) {
            _motionView = value
            outerEffect.motionView = value
            innerEffect.motionView = value
        }

    override fun forFrame(frame: Int): MotionView {
        if (motionView !is View) return motionView
        val view = motionView as View

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return motionView

        if (frame !in startFrame..endFrame) {
            if (frame > endFrame) {
                view.setRenderEffect(null)
            }
            return motionView
        }

        // Chaining is tricky with the current side-effect based architecture.
        // For now, we just call the inner and outer effects, which will 
        // each try to set the RenderEffect on the view, with the last one winning.
        // To properly support chaining, we would need to refactor MotionEffect 
        // to return a RenderEffect instead of applying it.
        innerEffect.forFrame(frame)
        outerEffect.forFrame(frame)
        
        return motionView
    }
}
