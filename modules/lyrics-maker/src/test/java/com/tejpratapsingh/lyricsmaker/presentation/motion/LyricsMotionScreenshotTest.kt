package com.tejpratapsingh.lyricsmaker.presentation.motion

import androidx.core.graphics.toColorInt
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.paparazzi.snapshot
import com.tejpratapsingh.motionlib.paparazzi.snapshotFrames
import com.tejpratapsingh.motionlib.ui.custom.background.GradientView
import com.tejpratapsingh.motionlib.ui.custom.background.Orientation
import com.tejpratapsingh.motionlib.ui.custom.text.PopUpTextView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class LyricsMotionScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            theme = "Theme.AppCompat.Light.NoActionBar",
        )

    @Test
    fun testLyricsComposerViewSnapshot() {
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
                endFrame = 100,
                orientation = Orientation.VERTICAL,
                colors =
                    intArrayOf(
                        "#2568ff".toColorInt(),
                        "#7048ff".toColorInt(),
                        "#ba28ff".toColorInt(),
                    ),
            )
        producer.addMotionViewToSequence(gradientView)

        val textView =
            PopUpTextView(
                context = context,
                text = "Lyrics Maker",
                startFrame = 0,
                endFrame = 100,
            )
        producer.addMotionViewToSequence(textView)

        val bitmap =
            paparazzi.snapshot(
                composerView = producer.motionComposerView,
                frame = 100,
                name = "lyrics_maker_frame_20",
            )

        assertNotNull("Screenshot image bitmap should not be null", bitmap)
        assertEquals(1920, bitmap.width)
        assertEquals(1080, bitmap.height)
    }

    @Test
    fun testLyricsComposerViewSequenceSnapshot() {
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

        val gradientView =
            GradientView(
                context = context,
                startFrame = 0,
                endFrame = 100,
                orientation = Orientation.HORIZONTAL,
                colors = intArrayOf(0xFF000000.toInt(), 0xFF434343.toInt()),
            )
        producer.addMotionViewToSequence(gradientView)

        val bitmaps =
            paparazzi.snapshotFrames(
                composerView = producer.motionComposerView,
                frames = listOf(0, 25, 50),
                namePrefix = "lyrics_maker_sequence",
            )

        assertEquals(3, bitmaps.size)
        bitmaps.forEach { bitmap ->
            assertNotNull("Each frame screenshot bitmap should not be null", bitmap)
            assertEquals(1080, bitmap.width)
            assertEquals(1920, bitmap.height)
        }
    }
}
