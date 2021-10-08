package com.tejpratapsingh.motionlib.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class MotionInterpolatorTest {

    @Test
    fun `Assert linear interpolation`() {

        val expectedValue = 0.5F

        val currentFrame: Float = 110F
        val frameRange: Pair<Float, Float> = Pair(first = 100F, second = 120F)
        val valueRange: Pair<Float, Float> = Pair(first = 0F, second = 1F)
        val interpolatedValue =
            MotionInterpolator.interpolateForRange(MotionInterpolator.Companion.Linear.easeNone, currentFrame, frameRange, valueRange)

        assertEquals(
            expectedValue,
            interpolatedValue
        )
    }
}