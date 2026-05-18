package com.tejpratapsingh.motion.sdui

import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.infra.MotionSduiInitializer
import com.tejpratapsingh.motion.sdui.infra.toMotionView
import com.tejpratapsingh.motionlib.ui.custom.background.GradientView
import com.tejpratapsingh.motionlib.ui.custom.background.TranslucentMotionView
import com.tejpratapsingh.motionlib.ui.custom.text.TransparentTextView
import com.tejpratapsingh.motionlib.ui.custom.text.TypeWriterTextView
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class MotionSduiRegistryTest {
    @Before
    fun setup() {
        MotionSduiInitializer.initialize()
    }

    @Test
    fun testTransparentTextViewRegistration() {
        val json =
            JsonObject().apply {
                addProperty("type", "TransparentTextView")
                addProperty("text", "Hello")
                addProperty("startFrame", 0)
                addProperty("endFrame", 100)
            }
        val view = json.toMotionView(RuntimeEnvironment.getApplication())
        assertTrue(view is TransparentTextView)
    }

    @Test
    fun testTypeWriterTextViewRegistration() {
        val json =
            JsonObject().apply {
                addProperty("type", "TypeWriterTextView")
                addProperty("text", "Hello")
                addProperty("startFrame", 0)
                addProperty("endFrame", 100)
                addProperty("writingSpeed", 1.0f)
            }
        val view = json.toMotionView(RuntimeEnvironment.getApplication())
        assertTrue(view is TypeWriterTextView)
    }

    @Test
    fun testGradientViewRegistration() {
        val json =
            JsonObject().apply {
                addProperty("type", "GradientView")
                addProperty("startFrame", 0)
                addProperty("endFrame", 100)
                addProperty("orientation", "HORIZONTAL")
            }
        val view = json.toMotionView(RuntimeEnvironment.getApplication())
        assertTrue(view is GradientView)
    }

    @Test
    fun testTranslucentMotionViewRegistration() {
        val json =
            JsonObject().apply {
                addProperty("type", "TranslucentMotionView")
                addProperty("color", "#FF0000")
                addProperty("alpha", 0.5f)
                addProperty("startFrame", 0)
                addProperty("endFrame", 100)
            }
        val view = json.toMotionView(RuntimeEnvironment.getApplication())
        assertTrue(view is TranslucentMotionView)
        assertTrue((view as TranslucentMotionView).alpha == 0.5f)
    }
}
