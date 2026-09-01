package com.tejpratapsingh.motionlib.core

/**
 * Text size variants for video overlays.
 */
enum class MotionTextVariant {
    H1,
    H2,
    H3,
    H4,
    H5,
    H6,
    P,
}

/**
 * Collection of text sizes for a specific aspect ratio.
 */
internal data class MotionTextSizes(
    val h1: Float,
    val h2: Float,
    val h3: Float,
    val h4: Float,
    val h5: Float,
    val h6: Float,
    val p: Float,
)

/**
 * Provider for text sizes based on [VideoAspectRatio].
 * Provides scaled font sizes in pixels relative to the video dimensions.
 */
object MotionTextSizeProvider {
    /**
     * Returns the [MotionTextSizes] for the given [aspectRatio] and [baseTextScale].
     *
     * Returns null for [VideoAspectRatio.Custom] as requested.
     */
    internal fun getTextSizes(
        aspectRatio: VideoAspectRatio,
        baseTextScale: Float,
    ): MotionTextSizes? {
        if (aspectRatio is VideoAspectRatio.Custom) {
            return null
        }

        return MotionTextSizes(
            h1 = aspectRatio.scale(160f * baseTextScale),
            h2 = aspectRatio.scale(120f * baseTextScale),
            h3 = aspectRatio.scale(80f * baseTextScale),
            h4 = aspectRatio.scale(60f * baseTextScale),
            h5 = aspectRatio.scale(40f * baseTextScale),
            h6 = aspectRatio.scale(32f * baseTextScale),
            p = aspectRatio.scale(48f * baseTextScale),
        )
    }

    /**
     * Returns the font size in pixels for the given [variant], [aspectRatio] and [baseTextScale].
     *
     * For [VideoAspectRatio.Custom], it returns a fallback size of (32.0f * baseTextScale).
     */
    fun getFontSize(
        aspectRatio: VideoAspectRatio,
        variant: MotionTextVariant,
        baseTextScale: Float,
    ): Float {
        val sizes = getTextSizes(aspectRatio, baseTextScale) ?: return aspectRatio.scale(32f * baseTextScale)

        return when (variant) {
            MotionTextVariant.H1 -> sizes.h1
            MotionTextVariant.H2 -> sizes.h2
            MotionTextVariant.H3 -> sizes.h3
            MotionTextVariant.H4 -> sizes.h4
            MotionTextVariant.H5 -> sizes.h5
            MotionTextVariant.H6 -> sizes.h6
            MotionTextVariant.P -> sizes.p
        }
    }
}

/**
 * Extension to easily get [MotionTextSizes] from [MotionConfig].
 */
internal val MotionConfig.textSizes: MotionTextSizes?
    get() = MotionTextSizeProvider.getTextSizes(this.aspectRatio, this.baseTextScale)

/**
 * Extension to easily get a specific text size from [MotionConfig].
 */
fun MotionConfig.getFontSize(variant: MotionTextVariant): Float =
    MotionTextSizeProvider.getFontSize(this.aspectRatio, variant, this.baseTextScale)

/**
 * Convenience properties that automatically use the current [MotionConfig].
 */
val MotionConfig.fontSizeH1: Float
    get() = this.getFontSize(MotionTextVariant.H1)

val MotionConfig.fontSizeH2: Float
    get() = this.getFontSize(MotionTextVariant.H2)

val MotionConfig.fontSizeH3: Float
    get() = this.getFontSize(MotionTextVariant.H3)

val MotionConfig.fontSizeH4: Float
    get() = this.getFontSize(MotionTextVariant.H4)

val MotionConfig.fontSizeH5: Float
    get() = this.getFontSize(MotionTextVariant.H5)

val MotionConfig.fontSizeH6: Float
    get() = this.getFontSize(MotionTextVariant.H6)

val MotionConfig.fontSizeP: Float
    get() = this.getFontSize(MotionTextVariant.P)
