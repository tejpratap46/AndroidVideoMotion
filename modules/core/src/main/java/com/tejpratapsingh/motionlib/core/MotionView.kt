package com.tejpratapsingh.motionlib.core

import android.graphics.Bitmap

interface MotionView : OnMotionFrameListener {
    /**
     * The frame at which this view should start being rendered.
     */
    val startFrame: Int

    /**
     * The frame at which this view should stop being rendered.
     */
    val endFrame: Int

    /**
     * Defines the loop range [start, end] for assets like video that can loop.
     */
    val loop: Pair<Int, Int>

    /**
     * List of effects to be applied to this view.
     */
    val effects: List<MotionEffect>

    /**
     * Layout information for this view, including dimensions, padding, and gravity.
     */
    var layoutInfo: MotionLayoutInfo
        get() = MotionLayoutInfo()
        set(value) {}

    /**
     * List of assets required by this view.
     * These assets should be prepared before rendering the view.
     */
    val assets: List<MotionAsset>
        get() = emptyList()

    /**
     * Adds an effect to this view.
     */
    fun addEffect(effect: MotionEffect)

    /**
     * Returns the bitmap representation of this view for the current frame.
     */
    fun getViewBitmap(): Bitmap
}
