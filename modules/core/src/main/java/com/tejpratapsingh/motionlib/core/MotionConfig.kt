package com.tejpratapsingh.motionlib.core

data object MotionConfig {
    var aspectRatio: VideoAspectRatio = VideoAspectRatio.Ratio9x16_480
    var fps: Int = 24
    var outputQuality: Int = 100

    operator fun invoke(
        aspectRatio: VideoAspectRatio = VideoAspectRatio.Ratio9x16_480,
        fps: Int = 24,
        outputQuality: Int = 100,
    ): MotionConfig {
        this.aspectRatio = aspectRatio
        this.fps = fps
        this.outputQuality = outputQuality
        return this
    }
}
