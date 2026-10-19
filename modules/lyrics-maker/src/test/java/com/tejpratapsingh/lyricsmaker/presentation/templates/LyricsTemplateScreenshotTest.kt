package com.tejpratapsingh.lyricsmaker.presentation.templates

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.paparazzi.snapshot
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate
import com.tejpratapsingh.motionlib.templates.model.TemplateData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class LyricsTemplateScreenshotTest {

    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            theme = "Theme.AppCompat.Light.NoActionBar",
        )

    private val mockLyrics =
        listOf(
            SyncedLyricFrame(frame = 0, text = "First line of lyrics"),
            SyncedLyricFrame(frame = 30, text = "Second line of lyrics"),
            SyncedLyricFrame(frame = 60, text = "Third line of lyrics"),
            SyncedLyricFrame(frame = 90, text = "End of lyrics"),
        )

    private val templateData =
        TemplateData(
            mapOf(
                "songName" to "Sample Song",
                "lyrics" to mockLyrics,
                "primaryColor" to "#2568FF",
                "secondaryColor" to "#BA28FF",
            ),
        )

    private fun assertTemplateSnapshot(
        template: MotionTemplate,
        frame: Int = 35,
        nameOverride: String? = null,
    ) {
        val context = paparazzi.context
        val motionConfig = MotionConfig(aspectRatio = VideoAspectRatio.Ratio9x16_1080)
        val producer = MotionVideoProducer.with(context = context, motionConfig = motionConfig)
        val scope = ContentScope(context = context, producer = producer, data = templateData)

        template.buildContent(scope)

        val sanitizeName = nameOverride ?: "template_${template.name.lowercase().replace(" ", "_")}"
        val bitmap =
            paparazzi.snapshot(
                composerView = producer.motionComposerView,
                frame = frame,
                name = sanitizeName,
            )

        assertNotNull("Bitmap for template ${template.name} should not be null", bitmap)
        assertEquals(1080, bitmap.width)
        assertEquals(1920, bitmap.height)
    }

    @Test
    fun testPopupLyricsTemplate() {
        assertTemplateSnapshot(PopupLyricsTemplate)
    }

    @Test
    fun testTypewriterLyricsTemplate() {
        assertTemplateSnapshot(TypewriterLyricsTemplate)
    }

    @Test
    fun testWordwriterLyricsTemplate() {
        assertTemplateSnapshot(WordwriterLyricsTemplate)
    }

    @Test
    fun testGlitchLyricsTemplate() {
        assertTemplateSnapshot(GlitchLyricsTemplate)
    }

    @Test
    fun testZoomLyricsTemplate() {
        assertTemplateSnapshot(ZoomLyricsTemplate)
    }

    @Test
    fun testVibrateLyricsTemplate() {
        assertTemplateSnapshot(VibrateLyricsTemplate)
    }

    @Test
    fun testWordVibrateLyricsTemplate() {
        assertTemplateSnapshot(WordVibrateLyricsTemplate)
    }

    @Test
    fun testGradientLyricsTemplate() {
        assertTemplateSnapshot(GradientLyricsTemplate)
    }

    @Test
    fun testRainbowLyricsTemplate() {
        assertTemplateSnapshot(RainbowLyricsTemplate)
    }

    @Test
    fun testAccentLyricsTemplate() {
        assertTemplateSnapshot(AccentLyricsTemplate)
    }

    @Test
    fun testVintageLyricsTemplate() {
        assertTemplateSnapshot(VintageLyricsTemplate)
    }

    @Test
    fun testStackedLyricsTemplate() {
        assertTemplateSnapshot(StackedLyricsTemplate)
    }
}
