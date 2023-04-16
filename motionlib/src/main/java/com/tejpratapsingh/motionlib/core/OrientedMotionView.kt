package com.tejpratapsingh.motionlib.core

import android.content.Context

open class OrientedMotionView(
    context: Context,
    startFrame: Int,
    endFrame: Int,
    orientation: Orientation = Orientation.VERTICAL
) : MotionView(context, startFrame, endFrame) {
}