package com.tejpratapsingh.motionlib.core

import android.view.View
import android.view.ViewParent

data class MotionConfig(
    val aspectRatio: VideoAspectRatio = VideoAspectRatio.Ratio9x16_480,
    val fps: Int = 24,
    val outputQuality: Int = 100,
    val baseTextScale: Float = 1.5f,
)

/**
 * Extension to find the [MotionConfig] from the view hierarchy.
 * Traverses up the parent tree until it finds a [MotionConfigProvider].
 */
fun View.findMotionConfig(): MotionConfig {
    if (this is MotionConfigProvider) return this.motionConfig

    var currentParent: ViewParent? = this.parent
    while (currentParent != null) {
        if (currentParent is MotionConfigProvider) {
            return currentParent.motionConfig
        }
        currentParent = currentParent.parent
    }

    // Fallback to default config if not found in hierarchy
    return MotionConfig()
}

/**
 * Extension to find the [MotionConfig] for a [MotionView] from the view hierarchy.
 */
fun MotionView.findConfig(): MotionConfig = (this as? View)?.findMotionConfig() ?: MotionConfig()

/**
 * Extension to find the [MotionConfig] for an [MotionEffect] from its associated [MotionView].
 */
fun MotionEffect.findConfig(): MotionConfig = this.motionView.findConfig()
