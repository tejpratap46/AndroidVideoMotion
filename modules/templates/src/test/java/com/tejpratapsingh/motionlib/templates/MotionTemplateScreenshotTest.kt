package com.tejpratapsingh.motionlib.templates

import androidx.core.graphics.toColorInt
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.paparazzi.snapshot
import com.tejpratapsingh.motionlib.paparazzi.snapshotFrames
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope
import com.tejpratapsingh.motionlib.templates.dsl.motionTemplate
import com.tejpratapsingh.motionlib.templates.extensions.gradientView
import com.tejpratapsingh.motionlib.templates.extensions.popUpTextView
import com.tejpratapsingh.motionlib.templates.model.TemplateData
import com.tejpratapsingh.motionlib.ui.custom.background.Orientation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class MotionTemplateScreenshotTest {

    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            theme = "Theme.AppCompat.Light.NoActionBar",
        )

    @Test
    fun testMotionTemplateRenderingAndSnapshot() {
        val template =
            motionTemplate("Sample Motion Template") {
                parameters {
                    string("title", defaultValue = "Default Title")
                    string("startColor", defaultValue = "#1E3C72")
                    string("endColor", defaultValue = "#2A5298")
                }

                content {
                    val title = data.getString("title") ?: "Title"
                    val startColor = data.getString("startColor") ?: "#1E3C72"
                    val endColor = data.getString("endColor") ?: "#2A5298"

                    gradientView(
                        startFrame = 0,
                        endFrame = 100,
                        orientation = Orientation.VERTICAL,
                        colors = intArrayOf(startColor.toColorInt(), endColor.toColorInt()),
                    )

                    popUpTextView(
                        text = title,
                        startFrame = 0,
                        endFrame = 100,
                    )
                }
            }

        val context = paparazzi.context
        val motionConfig =
            MotionConfig(
                aspectRatio = VideoAspectRatio.Ratio9x16_1080,
            )

        val producer =
            MotionVideoProducer.with(
                context = context,
                motionConfig = motionConfig,
            )

        val templateData =
            TemplateData(
                mapOf(
                    "title" to "Awesome Motion Template",
                    "startColor" to "#2568ff",
                    "endColor" to "#ba28ff",
                ),
            )

        val scope = ContentScope(context = context, producer = producer, data = templateData)
        template.buildContent(scope)

        val bitmap =
            paparazzi.snapshot(
                composerView = producer.motionComposerView,
                frame = 30,
                name = "motion_template_frame_30",
            )

        assertNotNull("Generated template bitmap should not be null", bitmap)
        assertEquals(1080, bitmap.width)
        assertEquals(1920, bitmap.height)
    }

    @Test
    fun testMotionTemplateSequenceSnapshot() {
        val template =
            motionTemplate("Sequence Motion Template") {
                parameters {
                    string("message", defaultValue = "Hello World")
                }

                content {
                    val message = data.getString("message") ?: "Hello World"

                    gradientView(
                        startFrame = 0,
                        endFrame = 100,
                        orientation = Orientation.HORIZONTAL,
                        colors = intArrayOf("#000000".toColorInt(), "#434343".toColorInt()),
                    )

                    popUpTextView(
                        text = message,
                        startFrame = 0,
                        endFrame = 100,
                    )
                }
            }

        val context = paparazzi.context
        val motionConfig =
            MotionConfig(
                aspectRatio = VideoAspectRatio.Ratio9x16_1080,
            )

        val producer =
            MotionVideoProducer.with(
                context = context,
                motionConfig = motionConfig,
            )

        val templateData =
            TemplateData(
                mapOf("message" to "Paparazzi Template Animation"),
            )

        val scope = ContentScope(context = context, producer = producer, data = templateData)
        template.buildContent(scope)

        val bitmaps =
            paparazzi.snapshotFrames(
                composerView = producer.motionComposerView,
                frames = listOf(0, 25, 50),
                namePrefix = "template_sequence",
            )

        assertEquals(3, bitmaps.size)
        bitmaps.forEach { bitmap ->
            assertNotNull(bitmap)
            assertEquals(1080, bitmap.width)
            assertEquals(1920, bitmap.height)
        }
    }
}
