package com.tejpratapsingh.motionlib.ui

import android.content.Context
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.utils.MotionConfig

open class MotionComposerView(context: Context, val motionConfig: MotionConfig) :
    ContourLayout(context), OnMotionFrameListener {
    private val TAG = "MotionComposerView"

    init {
        this.layout(0, 0, motionConfig.width, motionConfig.height)
    }

    override fun forFrame(frame: Int) {
        for (i in 0..this.childCount) {
            val view = this.getChildAt(i)

            if (view is MotionView) {
                view.forFrame(frame)
            }
        }
    }
}