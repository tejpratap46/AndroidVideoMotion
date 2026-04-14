package com.tejpratapsingh.motionlib.core.motion

import android.content.Context
import timber.log.Timber
import androidx.annotation.CallSuper
import androidx.core.view.isVisible
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.toBitmap

open class BaseContourMotionView(
    context: Context,
    override val startFrame: Int,
    override val endFrame: Int,
    override val loop: Pair<Int, Int> = Pair(0, 0),
    override val effects: List<MotionEffect> = emptyList(),
) : ContourLayout(context),
    MotionView {

    @CallSuper
    override fun forFrame(frame: Int): MotionView {
        if (frame < startFrame) {
            visibility = INVISIBLE
            return this
        }
        if (frame > endFrame) {
            visibility = INVISIBLE
            return this
        }
        visibility = VISIBLE

        Timber.d("forFrame: isVisible: $isVisible")

        for (i in 0..this.childCount) {
            val view = this.getChildAt(i)

            if (view is MotionView) {
                view.forFrame(frame)
            }
        }

        return this
    }

    override fun getViewBitmap() = this.toBitmap()
}
