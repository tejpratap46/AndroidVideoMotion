package com.tejpratapsingh.motionlib.ui

import android.content.Context
import android.util.Log
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.utils.MotionConfig

interface OnMotionFrameListener {
    fun forFrame(frame: Int)
}

enum class Orientation {
    HORIZONTAL,
    VERTICAL,
    CIRCULAR
}

open class MotionView(
    context: Context,
    startFrame: Int,
    endFrame: Int,
    orientation: Orientation = Orientation.VERTICAL
) :
    ContourLayout(context), OnMotionFrameListener {
    private val TAG = "MotionView"

    var startFrame = startFrame
        get() = field
        private set(value) {
            field = value
        }

    var endFrame = endFrame
        get() = field
        private set(value) {
            field = value
        }

    lateinit var motionConfig: MotionConfig

    override fun forFrame(frame: Int) {
        if (frame < startFrame) {
            visibility = INVISIBLE
            return
        }
        if (frame > endFrame) {
            visibility = INVISIBLE
            return
        }
        visibility = VISIBLE

        Log.d(TAG, "forFrame: isVisible: ${visibility == VISIBLE}")

        for (i in 0..this.childCount) {
            val view = this.getChildAt(i)

            if (view is MotionView) {
                view.forFrame(frame)
            }
        }
    }

//    suspend fun forFrameSuspended(frame: Int): Bitmap = suspendCoroutine { cont ->
//        if (frame < startFrame) {
//            visibility = INVISIBLE
//            cont.resume(Utilities.getViewBitmap(this@MotionView))
//        }
//        if (frame > endFrame) {
//            visibility = INVISIBLE
//            cont.resume(Utilities.getViewBitmap(this@MotionView))
//        }
//        visibility = VISIBLE
//
//        Log.d(TAG, "forFrame: isVisible: ${visibility == VISIBLE}")
//
//        viewTreeObserver.addOnGlobalLayoutListener {
//            cont.resume(Utilities.getViewBitmap(this@MotionView))
//        }
//
//        for (i in 0..this.childCount) {
//            val view = this.getChildAt(i)
//
//            if (view is MotionView) {
//                supervisorScope {
//                    launch {
//                        view.forFrameSuspended(frame)
//                    }
//                }
//            }
//        }
//    }
}