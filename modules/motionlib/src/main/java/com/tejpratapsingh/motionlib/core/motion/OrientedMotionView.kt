package com.tejpratapsingh.motionlib.core.motion

import android.content.Context
import com.tejpratapsingh.motionlib.core.MotionEffect

open class OrientedMotionView(
    context: Context,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
) : BaseContourMotionView(context, startFrame, endFrame, effects = effects)
