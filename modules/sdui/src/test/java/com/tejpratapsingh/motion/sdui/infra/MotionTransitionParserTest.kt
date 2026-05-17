package com.tejpratapsingh.motion.sdui.infra

import com.tejpratapsingh.motionlib.core.motion.transitions.BlurTransition
import com.tejpratapsingh.motionlib.core.motion.transitions.CrossFadeTransition
import com.tejpratapsingh.motionlib.core.motion.transitions.SlideDirection
import com.tejpratapsingh.motionlib.core.motion.transitions.SlideTransition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MotionTransitionParserTest {
    @Before
    fun setup() {
        // We need to initialize the registry
        MotionSduiInitializer.initialize()
    }

    @Test
    fun testCrossFadeSerialization() {
        val transition = CrossFadeTransition()
        val json = transition.toJson()

        assertEquals("CrossFadeTransition", json.get("type").asString)

        val deserialized = json.toMotionTransition()
        assertTrue(deserialized is CrossFadeTransition)
    }

    @Test
    fun testBlurSerialization() {
        val transition = BlurTransition(maxBlurRadius = 15f)
        val json = transition.toJson()

        assertEquals("BlurTransition", json.get("type").asString)
        assertEquals(15f, json.get("maxBlurRadius").asFloat, 0.01f)

        val deserialized = json.toMotionTransition()
        assertTrue(deserialized is BlurTransition)
        assertEquals(15f, (deserialized as BlurTransition).maxBlurRadius, 0.01f)
    }

    @Test
    fun testSlideSerialization() {
        val transition = SlideTransition(direction = SlideDirection.TOP_TO_BOTTOM)
        val json = transition.toJson()

        assertEquals("SlideTransition", json.get("type").asString)
        assertEquals("TOP_TO_BOTTOM", json.get("direction").asString)

        val deserialized = json.toMotionTransition()
        assertTrue(deserialized is SlideTransition)
        assertEquals(SlideDirection.TOP_TO_BOTTOM, (deserialized as SlideTransition).direction)
    }
}
