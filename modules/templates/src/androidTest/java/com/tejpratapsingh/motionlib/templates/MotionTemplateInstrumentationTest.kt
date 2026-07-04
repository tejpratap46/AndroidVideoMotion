package com.tejpratapsingh.motionlib.templates

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope
import com.tejpratapsingh.motionlib.templates.dsl.motionTemplate
import com.tejpratapsingh.motionlib.templates.extensions.popUpTextView
import com.tejpratapsingh.motionlib.templates.extensions.typeWriterTextView
import com.tejpratapsingh.motionlib.templates.extensions.videoFrameView
import com.tejpratapsingh.motionlib.templates.model.TemplateData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MotionTemplateInstrumentationTest {
    data class LyricLine(
        val text: String,
        val startFrame: Int,
        val endFrame: Int,
    )

    @Test
    fun testHardExampleWithRealContext() {
        val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

        // We need a real MotionVideoProducer for the content block to execute addView/audio calls
        val producer = MotionVideoProducer.with(appContext)

        val lyricsVideoTemplate =
            motionTemplate("Lyrics Master") {
                parameters {
                    string("songTitle")
                    string("backgroundVideoPath")
                }

                content {
                    val bgVideoPath = data.getString("backgroundVideoPath")
                    assertNotNull(bgVideoPath)

                    // Add a dummy background video view using the DSL extension
                    // Even if the path is fake, we're testing the DSL registration
                    videoFrameView(
                        videoUri = android.net.Uri.parse(bgVideoPath!!),
                        startFrame = 0,
                        endFrame = 1000,
                    )

                    // Fetch list of lyrics from data
                    val lyrics = data.get<List<LyricLine>>("lyricLines") ?: emptyList()
                    assertEquals(2, lyrics.size)

                    lyrics.forEach { line ->
                        // Use the DSL to add text views for lyrics
                        popUpTextView(
                            text = line.text,
                            startFrame = line.startFrame,
                            endFrame = line.endFrame,
                        )
                    }

                    // Overlay Song Title
                    val songTitle = data.getString("songTitle")
                    assertEquals("Amazing Grace", songTitle)

                    typeWriterTextView(
                        text = songTitle!!,
                        startFrame = 0,
                        endFrame = 60,
                    )

                    // Test audio function
                    audio(File(appContext.cacheDir, "test_audio.mp3"), startFrame = 0, endFrame = 500)
                }
            }

        assertEquals("Lyrics Master", lyricsVideoTemplate.name)

        // Mock data
        val lyricsData =
            listOf(
                LyricLine("Amazing grace how sweet the sound", 0, 100),
                LyricLine("That saved a wretch like me", 101, 200),
            )

        val data =
            TemplateData(
                mapOf(
                    "songTitle" to "Amazing Grace",
                    "backgroundVideoPath" to "/path/to/video.mp4",
                    "lyricLines" to lyricsData,
                ),
            )

        // Create the scope with REAL context and producer
        val scope = ContentScope(appContext, producer, data)

        // This will now execute the content block, including DSL extension calls
        lyricsVideoTemplate.buildContent(scope)

        // Verify that views and audio were actually added to the producer
        assertEquals(4, producer.motionComposerView.childCount) // 1 video + 2 lyrics + 1 title
        assertEquals(1, producer.motionAudio.size)
    }
}
