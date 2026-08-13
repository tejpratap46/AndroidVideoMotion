package com.tejpratapsingh.motionlib.ui.custom.stack

import com.tejpratapsingh.motionlib.core.MotionView

/**
 * Represents a section in a stack view.
 * @property view The [MotionView] to be displayed in this section.
 * @property percentage The percentage of the parent's dimension (width or height) this section should occupy.
 */
data class StackSection(
    val view: MotionView,
    val percentage: Float
)
