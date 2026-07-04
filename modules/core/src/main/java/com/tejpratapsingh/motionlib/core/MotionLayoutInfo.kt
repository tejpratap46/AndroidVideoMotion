package com.tejpratapsingh.motionlib.core

import android.view.Gravity
import android.view.ViewGroup

/**
 * Data class to store layout information for a [MotionView].
 * This information can be serialized to and from SDUI.
 */
data class MotionLayoutInfo(
    val width: Int = WRAP_CONTENT,
    val height: Int = WRAP_CONTENT,
    val padding: Padding = Padding(),
    val margin: Margin = Margin(),
    val gravity: Int = Gravity.NO_GRAVITY,
) {
    data class Padding(
        val left: Int = 0,
        val top: Int = 0,
        val right: Int = 0,
        val bottom: Int = 0,
    )

    data class Margin(
        val left: Int = 0,
        val top: Int = 0,
        val right: Int = 0,
        val bottom: Int = 0,
    )

    companion object {
        const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
        const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT
    }
}
