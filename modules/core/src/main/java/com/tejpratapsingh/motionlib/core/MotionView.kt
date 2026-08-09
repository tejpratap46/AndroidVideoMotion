package com.tejpratapsingh.motionlib.core

import android.graphics.Bitmap

interface MotionView : OnMotionFrameListener {
    val startFrame: Int
    val endFrame: Int

    val loop: Pair<Int, Int>

    val effects: List<MotionEffect>

    val layoutInfo: MotionLayoutInfo
        get() = MotionLayoutInfo()

    fun addEffect(effect: MotionEffect)

    fun getViewBitmap(): Bitmap

    /**
     * Returns true if all required assets for this view are available in cache.
     */
    fun isCached(cacheManager: MotionCacheManager): Boolean = true
}
