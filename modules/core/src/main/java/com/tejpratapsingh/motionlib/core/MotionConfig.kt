package com.tejpratapsingh.motionlib.core

data class MotionConfig(
    val aspectRatio: VideoAspectRatio = VideoAspectRatio.Ratio9x16_480,
    val fps: Int = 24,
    val outputQuality: Int = 100,
) {
    init {
        if (MotionConfigStore.motionConfig == null) {
            MotionConfigStore.motionConfig = this
        }
    }
}

private object MotionConfigStore {
    @Volatile
    var motionConfig: MotionConfig? = null
}

fun setCurrentConfig(motionConfig: MotionConfig) {
    MotionConfigStore.motionConfig = motionConfig
}

fun provideCurrentConfig(): MotionConfig =
    if (MotionConfigStore.motionConfig != null) {
        MotionConfigStore.motionConfig!!
    } else {
        MotionConfig()
    }
