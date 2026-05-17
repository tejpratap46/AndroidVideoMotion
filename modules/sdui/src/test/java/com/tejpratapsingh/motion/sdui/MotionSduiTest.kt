package com.tejpratapsingh.motion.sdui

import android.content.Context
import android.graphics.Bitmap
import android.view.Gravity
import com.tejpratapsingh.motion.sdui.infra.MotionSdui
import com.tejpratapsingh.motion.sdui.infra.toJson
import com.tejpratapsingh.motion.sdui.infra.toLayoutInfo
import com.tejpratapsingh.motion.sdui.infra.toMotionConfig
import com.tejpratapsingh.motion.sdui.infra.toMotionPlugin
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

class MotionSduiTest {
    @Test
    fun testMotionConfigSerialization() {
        val config =
            MotionConfig(
                aspectRatio = VideoAspectRatio.Ratio16x9_720,
                fps = 30,
                outputQuality = 90,
            )

        val json = config.toJson()
        val restoredConfig = json.toMotionConfig()

        assertEquals(config.fps, restoredConfig.fps)
        assertEquals(config.outputQuality, restoredConfig.outputQuality)
        assertEquals(config.aspectRatio.width, restoredConfig.aspectRatio.width)
        assertEquals(config.aspectRatio.height, restoredConfig.aspectRatio.height)
    }

    @Test
    fun testMotionPluginSerialization() {
        class MockPlugin(
            val value: String,
        ) : MotionPlugin {
            override fun apply(input: Bitmap): Bitmap = input
        }

        MotionSdui.registerPlugin(MockPlugin::class.java.simpleName) { _, json ->
            MockPlugin(json.get("value").asString)
        }
        MotionSdui.registerPluginSerializer(MockPlugin::class.java) { plugin, json ->
            json.addProperty("type", plugin.javaClass.simpleName)
            json.addProperty("value", plugin.value)
        }

        val plugin = MockPlugin("test-plugin")
        val json = plugin.toJson()

        // Passing mock context as MockPlugin doesn't use it.
        // This keeps the unit test lean and focused on logic.
        val restoredPlugin = json.toMotionPlugin(mock(Context::class.java))

        assertEquals(MockPlugin::class.java.simpleName, json.get("type").asString)
        assertEquals("test-plugin", (restoredPlugin as MockPlugin).value)
    }

    @Test
    fun testMotionLayoutInfoSerialization() {
        val layoutInfo =
            MotionLayoutInfo(
                width = MotionLayoutInfo.MATCH_PARENT,
                height = 500,
                padding = MotionLayoutInfo.Padding(10, 20, 30, 40),
                margin = MotionLayoutInfo.Margin(5, 5, 5, 5),
                gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP,
            )

        val json = layoutInfo.toJson()
        val restored = json.toLayoutInfo()

        assertEquals(MotionLayoutInfo.MATCH_PARENT, restored.width)
        assertEquals(500, restored.height)
        assertEquals(10, restored.padding.left)
        assertEquals(20, restored.padding.top)
        assertEquals(30, restored.padding.right)
        assertEquals(40, restored.padding.bottom)
        assertEquals(5, restored.margin.left)
        assertEquals(Gravity.CENTER_HORIZONTAL or Gravity.TOP, restored.gravity)

        assertEquals("match_parent", json.get("width").asString)
        assertEquals("center_horizontal|top", json.get("gravity").asString)
    }
}
