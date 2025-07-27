package com.tejpratapsingh.motionlib.core.motion

import android.content.Context
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.core.IMotionView
import com.tejpratapsingh.motionlib.core.MotionConfig

open class MotionView(
    context: Context,
    val startFrame: Int,
    val endFrame: Int
) : ContourLayout(context), IMotionView {
    companion object {
        private const val TAG = "MotionView"
    }

    // object will be available at the time of processing video
    lateinit var motionConfig: MotionConfig

    override fun forFrame(frame: Int): View {
        if (frame < startFrame) {
            visibility = INVISIBLE
            return this
        }
        if (frame > endFrame) {
            visibility = INVISIBLE
            return this
        }
        visibility = VISIBLE

        Log.d(TAG, "forFrame: isVisible: $isVisible")

        for (i in 0..this.childCount) {
            val view = this.getChildAt(i)

            if (view is IMotionView) {
                view.forFrame(frame)
            }
        }

        return this
    }
}