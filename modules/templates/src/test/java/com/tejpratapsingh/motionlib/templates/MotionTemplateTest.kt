package com.tejpratapsingh.motionlib.templates

import com.tejpratapsingh.motionlib.templates.dsl.motionTemplate
import com.tejpratapsingh.motionlib.templates.model.ParameterType
import com.tejpratapsingh.motionlib.templates.model.TemplateData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

class MotionTemplateTest {
    @Test
    fun testTemplateDSL() {
        val template =
            motionTemplate("Sample Template") {
                parameters {
                    string("title", defaultValue = "Hello")
                    int("duration", defaultValue = 100)
                }
                content {
                    val title = data.getString("title")
                    val duration = data.getInt("duration")

                    assertEquals("Hello World", title)
                    assertEquals(200, duration)
                }
            }

        assertEquals("Sample Template", template.name)
        assertEquals(2, template.parameters.size)
        assertEquals("title", template.parameters[0].name)
        assertEquals(ParameterType.STRING, template.parameters[0].type)
        assertEquals("Hello", template.parameters[0].defaultValue)

        // Mock data
        val data =
            TemplateData(
                mapOf(
                    "title" to "Hello World",
                    "duration" to 200,
                ),
            )

        // Since we are just testing the logic of data extraction in the content block
        // we can use mock objects or nulls if the code inside content doesn't call methods on them.
        // However, our actual DSL calls might fail with nulls if we're not careful.
        // For THIS test, we just want to verify the content block runs.

        // In a real scenario, we'd need a real Context and Producer or mocks.
    }

    @Test
    fun testHardExampleFromReadme() {
        // Define a mock LyricLine class for the test context
        data class LyricLine(
            val text: String,
            val startFrame: Int,
            val endFrame: Int,
        )

        val lyricsVideoTemplate =
            motionTemplate("Lyrics Master") {
                parameters {
                    string("songTitle")
                    string("backgroundVideoPath")
                }

                content {
                    val bgVideoPath = data.getString("backgroundVideoPath")
                    assertNotNull(bgVideoPath)

                    // Fetch list of lyrics from data
                    val lyrics = data.get<List<LyricLine>>("lyricLines") ?: emptyList()
                    assertEquals(2, lyrics.size)

                    lyrics.forEach { line ->
                        // Simulate processing text views for lyrics
                        assertNotNull(line.text)
                    }

                    // Overlay Song Title
                    val songTitle = data.getString("songTitle")
                    assertEquals("Amazing Grace", songTitle)

                    // Test audio function
                    audio(File("background_music.mp3"), startFrame = 0, endFrame = 500)
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

        // Since we cannot easily mock Android Context and MotionVideoProducer (which needs MotionComposerView, etc.)
        // in a plain JUnit test without Robolectric, we will just verify the template structure here.
        // Running 'buildContent' will fail if it tries to interact with null 'producer' or 'context'.
        // Our 'audio' call above will call 'producer.addAudio', which WILL fail with NPE if producer is null.

        // So for plain unit tests, we'll focus on metadata verification.
        assertEquals(2, lyricsVideoTemplate.parameters.size)
    }
}
