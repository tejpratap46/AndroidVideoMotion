package com.tejpratapsingh.motion.sdui

import android.content.Context
import android.graphics.Bitmap
import com.tejpratapsingh.motion.sdui.infra.MotionSdui
import com.tejpratapsingh.motion.sdui.infra.createMotionSDUIJson
import com.tejpratapsingh.motion.sdui.infra.getMotionConfig
import com.tejpratapsingh.motion.sdui.infra.getMotionPlugins
import com.tejpratapsingh.motion.sdui.infra.getMotionViews
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionstore.tables.MotionProject
import com.tejpratapsingh.motionstore.tables.SyncTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class MotionProjectLogicTest {
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        // Register Logic-only Mock implementations
        MotionSdui.registerView(MockLogicView::class.java.simpleName) { _, json ->
            val startFrame = json.get("startFrame").asInt
            val endFrame = json.get("endFrame").asInt
            MockLogicView(startFrame, endFrame)
        }
        MotionSdui.registerPlugin(MockLogicPlugin::class.java.simpleName) { _, _ ->
            MockLogicPlugin()
        }
    }

    @Test
    fun testMotionProjectSduiRoundTrip() {
        // 1. Create a MotionView
        val originalView = MockLogicView(0, 100)

        // 2. Create a MotionPlugin
        val originalPlugin = MockLogicPlugin()

        // 3. Create a MotionConfig
        val originalConfig =
            MotionConfig(
                aspectRatio = VideoAspectRatio.Ratio16x9_720,
                fps = 30,
                outputQuality = 80,
            )

        // 4. Create MotionProject
        val originalProject =
            MotionProject(
                id = "test-id",
                name = "Test Project",
                path = "/test-path",
                sdui =
                    createMotionSDUIJson(
                        views = listOf(originalView),
                        plugins = listOf(originalPlugin),
                        config = originalConfig,
                    ),
                syncTracker = SyncTracker(updatedBy = "test-device"),
            )

        // 5. Recreate MotionView from restored SDUI
        val restoredView = originalProject.sdui.getMotionViews(mockContext).first()
        val restoredPlugin = originalProject.sdui.getMotionPlugins(mockContext).first()
        val restoredConfig = originalProject.sdui.getMotionConfig()

        // 6. Verify MotionViews are same logic-wise
        assertEquals(originalView.startFrame, restoredView.startFrame)
        assertEquals(originalView.endFrame, restoredView.endFrame)
        assertEquals(originalView.loop, restoredView.loop)

        // 7. Verify MotionPlugin is restored
        assertEquals(originalPlugin.javaClass, restoredPlugin.javaClass)

        // 8. Verify MotionConfig is restored
        assertNotNull(restoredConfig)
        assertEquals(originalConfig.fps, restoredConfig!!.fps)
        assertEquals(originalConfig.outputQuality, restoredConfig.outputQuality)
        assertEquals(originalConfig.aspectRatio.width, restoredConfig.aspectRatio.width)
        assertEquals(originalConfig.aspectRatio.height, restoredConfig.aspectRatio.height)
    }

    class MockLogicPlugin : MotionPlugin {
        override fun apply(input: Bitmap): Bitmap = input
    }

    class MockLogicView(
        override val startFrame: Int,
        override val endFrame: Int,
        override val loop: Pair<Int, Int> = Pair(0, 0),
    ) : MotionView {
        override val effects: List<com.tejpratapsingh.motionlib.core.MotionEffect> = emptyList()

        override fun addEffect(effect: com.tejpratapsingh.motionlib.core.MotionEffect) {
            // Not needed for this test
        }

        override fun forFrame(frame: Int): MotionView = this

        override fun getViewBitmap(): Bitmap = throw UnsupportedOperationException()
    }
}
