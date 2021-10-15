package com.tejpratapsingh.animator.ui.view

import android.content.Context
import com.tejpratapsingh.motionlib.ui.MotionView
import com.tejpratapsingh.motionlib.utils.Easings
import com.tejpratapsingh.motionlib.utils.Interpolators

class ContourDevice(context: Context) : MotionView(context) {

    override fun forFrame(frame: Int) {
        super.forFrame(frame)
        this.apply {
//            translationX = frame.toFloat() * 2
//            translationY = frame.toFloat() * 2
//            translationZ = frame.toFloat() * 2

            val interpolatedValue =
                Interpolators(Easings.CUBIC_IN_OUT).getInterpolation(frame.toFloat() / 120f)

            print(interpolatedValue)

            scaleX = interpolatedValue * 10f
            scaleY = interpolatedValue * 10f

            rotation = interpolatedValue * 360f
        }
    }
}