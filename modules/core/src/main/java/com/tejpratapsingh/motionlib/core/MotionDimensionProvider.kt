package com.tejpratapsingh.motionlib.core

import kotlin.math.min

/**
 * Utility to scale dimension values based on the video aspect ratio.
 * This ensures that paddings, margins, and other sizes scale proportionally
 * with the video resolution.
 */
object MotionDimensionProvider {
    /**
     * Scales the given [dpValue] based on the [aspectRatio] and [referenceSize].
     *
     * We use the smaller dimension (width or height) to scale values consistently
     * across different orientations (Landscape, Portrait, Square).
     * Standard reference is 1080f (the minimum dimension of a Full HD 16:9 or 9:16 video).
     *
     * @param aspectRatio The aspect ratio to scale against.
     * @param dpValue The base value to be scaled.
     * @param referenceSize The reference dimension (default is 1080f).
     * @return The scaled value in pixels.
     */
    fun getScaledValue(
        aspectRatio: VideoAspectRatio,
        dpValue: Float,
        referenceSize: Float = 1080f,
    ): Float {
        val currentSize = min(aspectRatio.width, aspectRatio.height).toFloat()
        val scale = currentSize / referenceSize
        return dpValue * scale
    }
}

/**
 * Extension to scale a value based on the [VideoAspectRatio].
 */
fun VideoAspectRatio.scale(
    dpValue: Float,
    reference: Float = 1080f,
): Float = MotionDimensionProvider.getScaledValue(this, dpValue, reference)

/**
 * Convenience function to get a scaled value using the [MotionConfig].
 */
fun MotionConfig.scaledDp(
    value: Float,
    reference: Float = 1080f,
): Float = this.aspectRatio.scale(value, reference)
