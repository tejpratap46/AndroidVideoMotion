package com.tejpratapsingh.motionlib.ui

import android.content.Context
import com.squareup.contour.ContourLayout

interface OnMotionFrameListener {
    fun forFrame(frame: Int)
}

open class MotionView(context: Context) : ContourLayout(context), OnMotionFrameListener {
    override fun forFrame(frame: Int) {
        for (i in 0..this.childCount) {
            val view = this.getChildAt(i)

            if (view is MotionView) {
                view.forFrame(frame)
            }
        }
    }

}