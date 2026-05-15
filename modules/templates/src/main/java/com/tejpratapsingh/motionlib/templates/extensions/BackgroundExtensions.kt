package com.tejpratapsingh.motionlib.templates.extensions

import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope
import com.tejpratapsingh.motionlib.ui.custom.background.GradientView
import com.tejpratapsingh.motionlib.ui.custom.background.Orientation

fun ContentScope.gradientView(
    startFrame: Int,
    endFrame: Int,
    orientation: Orientation,
    colors: IntArray,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (GradientView.() -> Unit)? = null,
) = GradientView(context, startFrame, endFrame, orientation, colors, effects = effects)
    .apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }
