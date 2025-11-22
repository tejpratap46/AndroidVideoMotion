package com.tejpratapsingh.motionlib.core

sealed class VideoAspectRatio(val width: Int, val height: Int, val label: String) {

    // Square
    data object Ratio1x1_480 : VideoAspectRatio(480, 480, "1:1 SD")
    data object Ratio1x1_720 : VideoAspectRatio(720, 720, "1:1 HD")
    data object Ratio1x1_1080 : VideoAspectRatio(1080, 1080, "1:1 Full HD")

    // Classic TV 4:3
    data object Ratio4x3_480 : VideoAspectRatio(640, 480, "4:3 SD")
    data object Ratio4x3_576 : VideoAspectRatio(768, 576, "4:3 PAL SD")
    data object Ratio4x3_720 : VideoAspectRatio(960, 720, "4:3 HD")

    // Widescreen 16:9
    data object Ratio16x9_480 : VideoAspectRatio(854, 480, "16:9 SD")
    data object Ratio16x9_720 : VideoAspectRatio(1280, 720, "16:9 HD")
    data object Ratio16x9_1080 : VideoAspectRatio(1920, 1080, "16:9 Full HD")
    data object Ratio16x9_1440 : VideoAspectRatio(2560, 1440, "16:9 2K")
    data object Ratio16x9_2160 : VideoAspectRatio(3840, 2160, "16:9 4K")

    // Portrait (for mobile)
    data object Ratio9x16_480 : VideoAspectRatio(480, 854, "9:16 SD")
    data object Ratio9x16_720 : VideoAspectRatio(720, 1280, "9:16 HD")
    data object Ratio9x16_1080 : VideoAspectRatio(1080, 1920, "9:16 Full HD")

    // Cinema wide 21:9
    data object Ratio21x9_1080 : VideoAspectRatio(2520, 1080, "21:9 Full HD")
    data object Ratio21x9_2160 : VideoAspectRatio(5120, 2160, "21:9 5K")

    // Custom ratio with any pixel size
    data class Custom(
        val customWidth: Int,
        val customHeight: Int,
        val customLabel: String = "Custom"
    ) :
        VideoAspectRatio(customWidth, customHeight, customLabel)

    fun ratioString(): String = "${width}:${height}"

    fun aspect(): Float = width.toFloat() / height.toFloat()
}