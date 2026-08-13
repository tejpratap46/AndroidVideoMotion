package com.tejpratapsingh.motionlib.templates.extensions

import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope
import com.tejpratapsingh.motionlib.ui.custom.container.RotatingMotionView
import com.tejpratapsingh.motionlib.ui.custom.stack.StackSection
import com.tejpratapsingh.motionlib.ui.custom.stack.VerticalStackMotionView

fun ContentScope.rotatingMotionView(
    startFrame: Int,
    endFrame: Int,
    view: View,
    degreePerSecond: Float = 6f,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (RotatingMotionView.() -> Unit)? = null,
) = RotatingMotionView(context, startFrame, endFrame, view, degreePerSecond, effects = effects)
    .apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }

fun ContentScope.verticalStackMotionView(
    startFrame: Int,
    endFrame: Int,
    sections: List<StackSection>,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (VerticalStackMotionView.() -> Unit)? = null,
) = VerticalStackMotionView(context, startFrame, endFrame, sections, effects = effects)
    .apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }
