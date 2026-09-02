package com.tejpratapsingh.motionlib.core

/**
 * Interface for views or components that provide a [MotionConfig].
 * Usually implemented by the root [MotionComposerView].
 */
interface MotionConfigProvider {
    val motionConfig: MotionConfig
}
