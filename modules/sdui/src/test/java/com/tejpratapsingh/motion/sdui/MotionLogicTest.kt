package com.tejpratapsingh.motion.sdui

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.tejpratapsingh.motion.sdui.infra.MotionSdui
import com.tejpratapsingh.motion.sdui.infra.toJson
import com.tejpratapsingh.motion.sdui.infra.toMotionEffect
import com.tejpratapsingh.motion.sdui.infra.toMotionView
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionstore.infra.DeviceInfo
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Pure logic test that does not depend on Android UI classes (View, ViewGroup, etc.).
 * It uses mock implementations of MotionView and MotionEffect that don't inherit from Android Views.
 */
@RunWith(RobolectricTestRunner::class)
class MotionLogicTest {
    private lateinit var mockContext: Context

    class MockLogicView(
        override val startFrame: Int,
        override val endFrame: Int,
        override val loop: Pair<Int, Int> = Pair(0, 0),
        override val motionConfig: MotionConfig = MotionConfig(),
    ) : MotionView {
        private val _effects = mutableListOf<MotionEffect>()
        override val effects: List<MotionEffect> get() = _effects

        override fun addEffect(effect: MotionEffect) {
            _effects.add(effect)
        }

        override fun forFrame(frame: Int): MotionView = this
        override fun getViewBitmap(): Bitmap = throw UnsupportedOperationException()
    }

    class MockLogicEffect(
        override val startFrame: Int,
        override val endFrame: Int,
    ) : MotionEffect {
        override lateinit var motionView: MotionView
        override fun forFrame(frame: Int): MotionView = motionView
    }

    @Before
    fun setup() {
        mockContext = ApplicationProvider.getApplicationContext()
        DeviceInfo.initialize(mockContext)
        // Register Logic-only Mock implementations
        MotionSdui.registerView(MockLogicView::class.java.simpleName) { _, json ->
            val startFrame = json.get("startFrame").asInt
            val endFrame = json.get("endFrame").asInt
            MockLogicView(startFrame, endFrame)
        }

        MotionSdui.registerEffect(MockLogicEffect::class.java.simpleName) { json ->
            val startFrame = json.get("startFrame").asInt
            val endFrame = json.get("endFrame").asInt
            MockLogicEffect(startFrame, endFrame)
        }
    }

    @Test
    fun testMotionViewAndEffectRoundTrip() {
        val originalView = MockLogicView(10, 50)
        val originalEffect = MockLogicEffect(15, 45)
        originalView.addEffect(originalEffect)

        val json = originalView.toJson()
        val restoredView = json.toMotionView(mockContext)

        assertEquals(10, restoredView.startFrame)
        assertEquals(50, restoredView.endFrame)

        assertEquals(1, restoredView.effects.size)
        val restoredEffect = restoredView.effects[0]
        assertEquals(15, restoredEffect.startFrame)
        assertEquals(45, restoredEffect.endFrame)
    }

    @Test
    fun testStandaloneEffectSerialization() {
        val effect = MockLogicEffect(5, 25)
        val json = effect.toJson()
        val restoredEffect = json.toMotionEffect()

        assertEquals(5, restoredEffect.startFrame)
        assertEquals(25, restoredEffect.endFrame)
    }
}
