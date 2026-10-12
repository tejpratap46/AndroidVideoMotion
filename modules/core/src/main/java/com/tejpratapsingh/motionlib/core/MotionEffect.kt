package com.tejpratapsingh.motionlib.core

interface MotionEffect : OnMotionFrameListener {
    var motionView: MotionView
    val startFrame: Int
    val endFrame: Int

    /**
     * Returns the list of editable property specifications for this effect.
     * Subclasses can override and extend this list using super.getEditableProperties() + customList.
     */
    fun getEditableProperties(): List<PropertySpec> {
        return listOf(
            PropertySpec.number("startFrame", label = "Start Frame", min = 0, max = 10000, isInteger = true),
            PropertySpec.number("endFrame", label = "End Frame", min = -1, max = 10000, isInteger = true)
        )
    }
}
