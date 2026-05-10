package com.tejpratapsingh.motion.sdui

import com.tejpratapsingh.motion.sdui.infra.MotionSdui
import com.tejpratapsingh.motion.sdui.infra.toJson
import com.tejpratapsingh.motion.sdui.infra.toMotionConfig
import com.tejpratapsingh.motion.sdui.infra.toMotionPlugin
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import org.junit.Assert.assertEquals
import org.junit.Test
import android.graphics.Bitmap
import android.content.Context
import org.mockito.Mockito.mock

class MotionSduiTest {

    @Test
    fun testMotionConfigSerialization() {
        val config = MotionConfig(
            aspectRatio = VideoAspectRatio.Ratio16x9_720,
            fps = 30,
            outputQuality = 90
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
        class MockPlugin(val value: String) : MotionPlugin {
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
}
