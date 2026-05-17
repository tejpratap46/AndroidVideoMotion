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
     * Base scale for all font sizes. Can be updated to scale all text sizes at once.
     */
    var baseTextScale: Float = 1.5f

    /**
     * Returns the [MotionTextSizes] for the given [aspectRatio].
     *
     * Returns null for [VideoAspectRatio.Custom] as requested.
     */
    internal fun getTextSizes(aspectRatio: VideoAspectRatio): MotionTextSizes? {
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
     * Returns the font size in pixels for the given [variant] and [aspectRatio].
     *
     * For [VideoAspectRatio.Custom], it returns a fallback size of (32.0f * baseTextScale).
     */
    fun getFontSize(
        aspectRatio: VideoAspectRatio,
        variant: MotionTextVariant,
    ): Float {
        val sizes = getTextSizes(aspectRatio) ?: return aspectRatio.scale(32f * baseTextScale)

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
 * Extension to easily get [MotionTextSizes] from [VideoAspectRatio].
 */
internal val VideoAspectRatio.textSizes: MotionTextSizes?
    get() = MotionTextSizeProvider.getTextSizes(this)

/**
 * Extension to easily get a specific text size from [VideoAspectRatio].
 */
internal fun VideoAspectRatio.getFontSize(variant: MotionTextVariant): Float = MotionTextSizeProvider.getFontSize(this, variant)

/**
 * Convenience properties that automatically use the current [MotionConfig]'s aspect ratio.
 */
val fontSizeH1: Float
    get() = provideCurrentConfig().aspectRatio.getFontSize(MotionTextVariant.H1)

val fontSizeH2: Float
    get() = provideCurrentConfig().aspectRatio.getFontSize(MotionTextVariant.H2)

val fontSizeH3: Float
    get() = provideCurrentConfig().aspectRatio.getFontSize(MotionTextVariant.H3)

val fontSizeH4: Float
    get() = provideCurrentConfig().aspectRatio.getFontSize(MotionTextVariant.H4)

val fontSizeH5: Float
    get() = provideCurrentConfig().aspectRatio.getFontSize(MotionTextVariant.H5)

val fontSizeH6: Float
    get() = provideCurrentConfig().aspectRatio.getFontSize(MotionTextVariant.H6)

val fontSizeP: Float
    get() = provideCurrentConfig().aspectRatio.getFontSize(MotionTextVariant.P)
