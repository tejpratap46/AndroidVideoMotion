package com.tejpratapsingh.motionlib.core

import android.content.Context
import android.util.Log
import android.view.View
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.utils.MotionConfig

enum class Orientation {
    HORIZONTAL,
    VERTICAL,
    CIRCULAR
}

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

        Log.d(TAG, "forFrame: isVisible: ${visibility == VISIBLE}")

        for (i in 0..this.childCount) {
            val view = this.getChildAt(i)

            if (view is IMotionView) {
                view.forFrame(frame)
            }
        }

        return this
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