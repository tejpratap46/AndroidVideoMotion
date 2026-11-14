package com.tejpratapsingh.motionlib.paparazzi

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.ui.custom.background.GradientView
import com.tejpratapsingh.motionlib.ui.custom.background.Orientation
import com.tejpratapsingh.motionlib.ui.effects.FadeInEffect
import org.junit.Rule
import org.junit.Test

class MotionComposerViewScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            theme = "Theme.AppCompat.Light.NoActionBar",
        )

    @Test
    fun testMotionComposerViewSingleFrame() {
        val context = paparazzi.context
        val motionConfig =
            MotionConfig(
                aspectRatio = VideoAspectRatio.Ratio16x9_1080,
            )

        val producer =
            MotionVideoProducer.with(
                context = context,
                motionConfig = motionConfig,
            )

        val gradientView =
            GradientView(
                context = context,
                startFrame = 0,
                endFrame = 60,
                orientation = Orientation.VERTICAL,
                colors = intArrayOf(0xFFFF0000.toInt(), 0xFF0000FF.toInt()),
            )
        gradientView.addEffect(FadeInEffect(startFrame = 0, endFrame = 20))

        producer.addMotionViewToSequence(gradientView)

        paparazzi.snapshot(composerView = producer.motionComposerView, frame = 10)
    }

    @Test
    fun testMotionComposerViewMultipleFrames() {
        val context = paparazzi.context
        val motionConfig =
            MotionConfig(
                aspectRatio = VideoAspectRatio.Ratio16x9_1080,
            )

        val producer =
            MotionVideoProducer.with(
                context = context,
                motionConfig = motionConfig,
            )

        val gradientView =
            GradientView(
                context = context,
                startFrame = 0,
                endFrame = 60,
                orientation = Orientation.HORIZONTAL,
                colors = intArrayOf(0xFF00FF00.toInt(), 0xFFFF00FF.toInt()),
            )
        producer.addMotionViewToSequence(gradientView)

        paparazzi.snapshotFrames(composerView = producer.motionComposerView, frames = listOf(0, 30, 60))
    }

    @Test
    fun testMotionComposerViewCustomWidthHeightInPx() {
        val context = paparazzi.context
        val customWidthPx = 720
        val customHeightPx = 1280

        val motionConfig =
            MotionConfig(
                aspectRatio = VideoAspectRatio.Custom(customWidthPx, customHeightPx),
            )

        val producer =
            MotionVideoProducer.with(
                context = context,
                motionConfig = motionConfig,
            )

        val gradientView =
            GradientView(
                context = context,
                startFrame = 0,
                endFrame = 60,
                orientation = Orientation.VERTICAL,
                colors = intArrayOf(0xFF123456.toInt(), 0xFF654321.toInt()),
            )
        producer.addMotionViewToSequence(gradientView)

        paparazzi.snapshot(
            composerView = producer.motionComposerView,
            frame = 15,
            widthPx = customWidthPx,
            heightPx = customHeightPx,
            name = "custom_px_frame_15",
        )
    }
}
