package com.tejpratapsingh.motion.sdui

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import com.tejpratapsingh.motion.sdui.infra.MotionSdui
import com.tejpratapsingh.motion.sdui.infra.SDUIMotionVideoProducerFactory
import com.tejpratapsingh.motion.sdui.infra.createMotionSDUIJson
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionstore.tables.MotionProject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SDUIMotionVideoProducerFactoryTest {
    private lateinit var mockContext: Context
    private lateinit var mockAdapter: VideoProducerAdapter

    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        mockAdapter = mock(VideoProducerAdapter::class.java)

        // Register Mock implementations
        MotionSdui.registerView("MockViewGroupView") { context, json ->
            val startFrame = json.get("startFrame").asInt
            val endFrame = json.get("endFrame").asInt
            MockViewGroupView(context, startFrame, endFrame)
        }
        MotionSdui.registerPlugin("MockPlugin") { _, _ ->
            MockPlugin()
        }
    }

    @Test
    fun testCreateFromSdui() {
        val sdui =
            createMotionSDUIJson(
                views = listOf(MockViewGroupView(mockContext, 0, 100)),
                plugins = listOf(MockPlugin()),
                config =
                    MotionConfig(
                        aspectRatio = VideoAspectRatio.Ratio16x9_720,
                        fps = 30,
                        outputQuality = 80,
                    ),
            )

        val factory = SDUIMotionVideoProducerFactory(mockContext, mockAdapter)
        val producer = factory.createFromSdui(sdui)

        assertNotNull(producer)
        assertEquals(100, producer.totalFrames)
        assertEquals(1, producer.motionComposerView.plugins.size)
    }

    @Test
    fun testCreateFromProject() {
        val project =
            MotionProject(
                id = "test-id",
                name = "Test Project",
                path = "/test-path",
                sdui =
                    createMotionSDUIJson(
                        views = listOf(MockViewGroupView(mockContext, 0, 200)),
                        config = MotionConfig(fps = 24),
                    ),
            )

        val factory = SDUIMotionVideoProducerFactory(mockContext, mockAdapter)
        val producer = factory.createFromProject(project)

        assertNotNull(producer)
        assertEquals(200, producer.totalFrames)
    }

    @Test
    fun testCreateFromSduiWithCallback() {
        val sdui =
            createMotionSDUIJson(
                views = listOf(MockViewGroupView(mockContext, 0, 100)),
                config = MotionConfig(fps = 30),
            )

        val factory = SDUIMotionVideoProducerFactory(mockContext, mockAdapter)
        var callbackInvoked = false
        val producer =
            factory.createFromSdui(sdui) { views ->
                callbackInvoked = true
                assertEquals(1, views.size)
                val effect = mock(com.tejpratapsingh.motionlib.core.MotionEffect::class.java)
                views[0].addEffect(effect)
            }

        assertNotNull(producer)
        assertEquals(true, callbackInvoked)
    }

    class MockPlugin : MotionPlugin {
        override fun apply(input: Bitmap): Bitmap = input
    }

    class MockViewGroupView(
        context: Context,
        override val startFrame: Int,
        override val endFrame: Int,
        override val loop: Pair<Int, Int> = Pair(0, 0),
    ) : ViewGroup(context),
        MotionView {
        override val effects: List<com.tejpratapsingh.motionlib.core.MotionEffect> = emptyList()

        override fun addEffect(effect: com.tejpratapsingh.motionlib.core.MotionEffect) {}

        override fun forFrame(frame: Int): MotionView = this

        override fun getViewBitmap(): Bitmap = throw UnsupportedOperationException()

        override fun onLayout(
            changed: Boolean,
            l: Int,
            t: Int,
            r: Int,
            b: Int,
        ) {}
    }
}
