package com.tejpratapsingh.motionlib.core

import android.content.Context
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.utils.MotionConfig

open class MotionView(
    context: Context,
    startFrame: Int,
    endFrame: Int
) :
    ContourLayout(context), IMotionView {
    private val TAG = "MotionView"

    var startFrame = startFrame
        private set

    var endFrame = endFrame
        private set

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