package com.tejpratapsingh.motionlib.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class MotionInterpolatorTest {

    @Test
    fun `Assert linear interpolation`() {

        val expectedValue = 0.5f

        val currentFrame: Float = 0.5f
        val frameRange: Pair<Float, Float> = Pair(first = 0f, second = 1f)
        val valueRange: Pair<Float, Float> = Pair(first = 0f, second = 1f)
        
        val interpolatedValue: Float =
            MotionInterpolator.interpolateForRange(
                MotionInterpolator.Companion.Linear.easeNone,
                currentFrame,
                frameRange,
                valueRange
            )

        assertEquals(
            expectedValue,
            interpolatedValue
        )
    }
}