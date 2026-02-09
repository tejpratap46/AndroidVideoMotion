package com.tejpratapsingh.motionlib.core.motion

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.toBitmap
import com.tejpratapsingh.motionlib.core.provideCurrentConfig

open class MotionComposerView(
    context: Context,
    override val startFrame: Int = 0,
    override val endFrame: Int = 0,
    override val plugins: List<MotionPlugin>,
    override val loop: Pair<Int, Int> = Pair(0, 0),
) : ContourLayout(context),
    MotionView,
    IComposerView {
    override val effects: List<MotionEffect> = emptyList()

    companion object {
        private const val TAG = "MotionComposerView"
    }

    init {
        val config: MotionConfig = provideCurrentConfig()
        this.layout(0, 0, config.aspectRatio.width, config.aspectRatio.height)
    }

    override fun forFrame(frame: Int): MotionView {
        Log.i(TAG, "forFrame: $frame")
        for (i in 0..this.childCount) {
            val view = this.getChildAt(i)

            if (view is MotionView) {
                view.forFrame(frame)
            }
        }

        return this
    }

    fun runEffects(
        view: MotionView,
        frame: Int,
    ) = view.effects.forEach { effect ->
        effect.forFrame(frame)
    }

    override fun getViewBitmap(): Bitmap =
        toBitmap().let {
            plugins.fold(it) { acc, plugin ->
                plugin.apply(acc)
            }
        }
}
