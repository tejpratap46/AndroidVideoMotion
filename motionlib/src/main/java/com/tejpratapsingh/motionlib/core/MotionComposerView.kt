package com.tejpratapsingh.motionlib.core

import android.content.Context
import android.view.View
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.utils.MotionConfig

open class MotionComposerView(context: Context, val motionConfig: MotionConfig) :
    ContourLayout(context), IMotionView {
        
    companion object {
        private const val TAG = "MotionComposerView"

    }

    init {
        this.layout(0, 0, motionConfig.width, motionConfig.height)
    }

    override fun forFrame(frame: Int): View {
        for (i in 0..this.childCount) {
            val view = this.getChildAt(i)

            if (view is MotionView) {
                view.forFrame(frame)
            }
        }

        return this
    }
}