package com.tejpratapsingh.motionlib.templates.extensions

import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope
import com.tejpratapsingh.motionlib.ui.custom.background.GradientView
import com.tejpratapsingh.motionlib.ui.custom.background.Orientation
import com.tejpratapsingh.motionlib.ui.custom.background.TranslucentMotionView

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

fun ContentScope.translucentMotionView(
    color: String,
    alpha: Float = 1.0f,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (TranslucentMotionView.() -> Unit)? = null,
) = TranslucentMotionView(context, color, alpha, startFrame, endFrame, effects = effects)
    .apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }
