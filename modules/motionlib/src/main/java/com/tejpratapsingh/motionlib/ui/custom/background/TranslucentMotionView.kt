package com.tejpratapsingh.motionlib.ui.custom.background

import android.content.Context
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.PropertySpec
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView

class TranslucentMotionView(
    context: Context,
    val color: String,
    alpha: Float = 1.0f,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
) : BaseContourMotionView(context, startFrame, endFrame, effects = effects) {
    init {
        setBackgroundColor(color.toColorInt())
        setAlpha(alpha)
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        return this
    }

    override fun getEditableProperties(): List<PropertySpec> =
        super.getEditableProperties() + listOf(
            PropertySpec.color("color", label = "Background Color"),
            PropertySpec.slider("alpha", label = "Opacity", min = 0f, max = 1f, step = 0.05f)
        )
}
