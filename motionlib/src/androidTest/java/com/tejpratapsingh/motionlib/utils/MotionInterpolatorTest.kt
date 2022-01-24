package com.tejpratapsingh.motionlib.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MotionInterpolatorTest {
    private val TAG = "MotionInterpolatorTest"

    @Test
    fun testInterpolator() {
        // Context of the app under test.
        val expectedValue = 5f

        val currentFrame = 5
        val frameRange: Pair<Int, Int> = Pair(first = 0, second = 10)
        val valueRange: Pair<Float, Float> = Pair(first = 0f, second = 10f)

        val interpolatedValue: Float =
            MotionInterpolator.interpolateForRange(
                Interpolators(Easings.LINEAR),
                currentFrame,
                frameRange,
                valueRange
            )

        println("Interpolated Value: $interpolatedValue")

        assertEquals(
            expectedValue,
            interpolatedValue
        )
    }
}