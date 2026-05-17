package com.tejpratapsingh.motion.sdui

import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.infra.MotionSduiInitializer
import com.tejpratapsingh.motion.sdui.infra.toMotionEffect
import com.tejpratapsingh.motionlib.ui.effects.*
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MotionEffectRegistrationTest {

    @Before
    fun setup() {
        MotionSduiInitializer.initialize()
    }

    @Test
    fun testSlideRightToLeftEffectRegistration() {
        val json = JsonObject().apply {
            addProperty("type", "SlideRightToLeftEffect")
            addProperty("startFrame", 0)
            addProperty("endFrame", 100)
        }
        val effect = json.toMotionEffect()
        assertTrue(effect is SlideRightToLeftEffect)
    }

    @Test
    fun testSlideLeftToRightEffectRegistration() {
        val json = JsonObject().apply {
            addProperty("type", "SlideLeftToRightEffect")
            addProperty("startFrame", 0)
            addProperty("endFrame", 100)
        }
        val effect = json.toMotionEffect()
        assertTrue(effect is SlideLeftToRightEffect)
    }

    @Test
    fun testSlideTopToBottomEffectRegistration() {
        val json = JsonObject().apply {
            addProperty("type", "SlideTopToBottomEffect")
            addProperty("startFrame", 0)
            addProperty("endFrame", 100)
        }
        val effect = json.toMotionEffect()
        assertTrue(effect is SlideTopToBottomEffect)
    }

    @Test
    fun testSlideBottomToTopEffectRegistration() {
        val json = JsonObject().apply {
            addProperty("type", "SlideBottomToTopEffect")
            addProperty("startFrame", 0)
            addProperty("endFrame", 100)
        }
        val effect = json.toMotionEffect()
        assertTrue(effect is SlideBottomToTopEffect)
    }

    @Test
    fun testZoomInEffectRegistration() {
        val json = JsonObject().apply {
            addProperty("type", "ZoomInEffect")
            addProperty("startFrame", 0)
            addProperty("endFrame", 100)
            addProperty("startScale", 1.0f)
            addProperty("endScale", 2.0f)
        }
        val effect = json.toMotionEffect()
        assertTrue(effect is ZoomInEffect)
    }

    @Test
    fun testZoomOutEffectRegistration() {
        val json = JsonObject().apply {
            addProperty("type", "ZoomOutEffect")
            addProperty("startFrame", 0)
            addProperty("endFrame", 100)
            addProperty("startScale", 2.0f)
            addProperty("endScale", 1.0f)
        }
        val effect = json.toMotionEffect()
        assertTrue(effect is ZoomOutEffect)
    }

    @Test
    fun testFadeInEffectRegistration() {
        val json = JsonObject().apply {
            addProperty("type", "FadeInEffect")
            addProperty("startFrame", 0)
            addProperty("endFrame", 100)
        }
        val effect = json.toMotionEffect()
        assertTrue(effect is FadeInEffect)
    }

    @Test
    fun testFadeOutEffectRegistration() {
        val json = JsonObject().apply {
            addProperty("type", "FadeOutEffect")
            addProperty("startFrame", 0)
            addProperty("endFrame", 100)
        }
        val effect = json.toMotionEffect()
        assertTrue(effect is FadeOutEffect)
    }

    @Test
    fun testBlurEffectRegistration() {
        val json = JsonObject().apply {
            addProperty("type", "BlurEffect")
            addProperty("startFrame", 0)
            addProperty("endFrame", 100)
            addProperty("maxBlurRadius", 20.0f)
        }
        val effect = json.toMotionEffect()
        assertTrue(effect is BlurEffect)
    }

    @Test
    fun testGlitchEffectRegistration() {
        val json = JsonObject().apply {
            addProperty("type", "GlitchEffect")
            addProperty("startFrame", 0)
            addProperty("endFrame", 100)
            addProperty("intensity", 10.0f)
        }
        val effect = json.toMotionEffect()
        assertTrue(effect is GlitchEffect)
    }

    @Test
    fun testVibrateEffectRegistration() {
        val json = JsonObject().apply {
            addProperty("type", "VibrateEffect")
            addProperty("startFrame", 0)
            addProperty("endFrame", 100)
            addProperty("amplitude", 5.0f)
            addProperty("frequency", 1.0f)
        }
        val effect = json.toMotionEffect()
        assertTrue(effect is VibrateEffect)
    }
}
