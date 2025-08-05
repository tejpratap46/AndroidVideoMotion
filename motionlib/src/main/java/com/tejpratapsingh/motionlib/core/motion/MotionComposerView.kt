package com.tejpratapsingh.motionlib.core.motion

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.toBitmap

open class MotionComposerView(
    context: Context, override var motionConfig: MotionConfig,
    override val startFrame: Int = 0,
    override val endFrame: Int = 0
) :
    ContourLayout(context), MotionView {

    companion object {
        private const val TAG = "MotionComposerView"

    }

    init {
        this.layout(0, 0, motionConfig.width, motionConfig.height)
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

    override fun getViewBitmap(): Bitmap {
        return toBitmap()
    }
}