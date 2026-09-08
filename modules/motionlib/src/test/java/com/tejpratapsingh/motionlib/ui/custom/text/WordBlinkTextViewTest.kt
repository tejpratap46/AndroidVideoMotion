package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WordBlinkTextViewTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testWordBlinkWritingSpeedBuffer() {
        val text = "Hello world example"
        val startFrame = 0
        val endFrame = 100
        val writingSpeed = 2f // inferredEndFrame will be 50

        val wordBlink = WordBlinkTextView(
            context = context,
            text = text,
            startFrame = startFrame,
            endFrame = endFrame,
            writingSpeed = writingSpeed,
        )

        // During writing phase (frames 0..49)
        wordBlink.forFrame(0)
        assertEquals("Hello", wordBlink.textView.text.toString())

        wordBlink.forFrame(20)
        assertEquals("world", wordBlink.textView.text.toString())

        wordBlink.forFrame(40)
        assertEquals("example", wordBlink.textView.text.toString())

        // At inferredEndFrame (frame 50), writing finishes and buffer phase begins -> full text
        wordBlink.forFrame(50)
        assertEquals("Hello world example", wordBlink.textView.text.toString())

        // During buffer phase (frames 51..100) -> full text
        wordBlink.forFrame(75)
        assertEquals("Hello world example", wordBlink.textView.text.toString())

        wordBlink.forFrame(100)
        assertEquals("Hello world example", wordBlink.textView.text.toString())
    }

    @Test
    fun testWordBlinkNormalSpeed() {
        val text = "One two three"
        val startFrame = 0
        val endFrame = 60
        val writingSpeed = 1f // inferredEndFrame = 60

        val wordBlink = WordBlinkTextView(
            context = context,
            text = text,
            startFrame = startFrame,
            endFrame = endFrame,
            writingSpeed = writingSpeed,
        )

        wordBlink.forFrame(0)
        assertEquals("One", wordBlink.textView.text.toString())

        wordBlink.forFrame(20)
        assertEquals("two", wordBlink.textView.text.toString())

        wordBlink.forFrame(40)
        assertEquals("three", wordBlink.textView.text.toString())

        wordBlink.forFrame(60)
        assertEquals("One two three", wordBlink.textView.text.toString())
    }
}
