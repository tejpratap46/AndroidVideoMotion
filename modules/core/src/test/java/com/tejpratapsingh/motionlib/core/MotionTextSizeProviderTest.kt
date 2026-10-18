package com.tejpratapsingh.motionlib.core

import org.junit.Assert.assertEquals
import org.junit.Test

class MotionTextSizeProviderTest {

    @Test
    fun testDefaultFontSizes() {
        val aspectRatio = VideoAspectRatio.Ratio16x9_1080 // 1920x1080, min is 1080, scale is 1.0

        // H1 should be 160f * 1.0 = 160f
        assertEquals(160f, MotionTextSizeProvider.getFontSize(aspectRatio, MotionTextVariant.H1, 1.0f), 0.01f)

        // P should be 48f * 1.0 = 48f
        assertEquals(48f, MotionTextSizeProvider.getFontSize(aspectRatio, MotionTextVariant.P, 1.0f), 0.01f)
    }

    @Test
    fun testBaseTextScale() {
        val aspectRatio = VideoAspectRatio.Ratio16x9_1080 // 1920x1080, min is 1080, scale is 1.0

        // H1 with baseTextScale 2.0 should be 160f * 2.0 = 320f
        assertEquals(320f, MotionTextSizeProvider.getFontSize(aspectRatio, MotionTextVariant.H1, 2.0f), 0.01f)

        // P with baseTextScale 2.0 should be 48f * 2.0 = 96f
        assertEquals(96f, MotionTextSizeProvider.getFontSize(aspectRatio, MotionTextVariant.P, 2.0f), 0.01f)
    }

    @Test
    fun testScalingWithAspectRatio() {
        // 480p 16:9 is 854x480, min is 480.
        // Scale = 480 / 1080 = 0.4444...
        val aspectRatio = VideoAspectRatio.Ratio16x9_480

        val expectedH1 = 160f * (480f / 1080f)
        assertEquals(expectedH1, MotionTextSizeProvider.getFontSize(aspectRatio, MotionTextVariant.H1, 1.0f), 0.01f)
    }
}
