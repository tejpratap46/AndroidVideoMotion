package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TypeWriterTextViewTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testBlinkingCursorVisibility() {
        val text = "Hello"
        val startFrame = 0
        val endFrame = 10
        val blinkFrameRate = 5
        val cursorChar = "|"
        
        val typeWriter = TypeWriterTextView(
            context = context,
            text = text,
            startFrame = startFrame,
            endFrame = endFrame,
            blinkFrameRate = blinkFrameRate,
            cursorChar = cursorChar
        )

        // Frame 0: (0 / 5) % 2 == 0 -> Show cursor
        // At frame 0, visibleCharsCount is 0
        typeWriter.forFrame(0)
        assertEquals("|Hello", typeWriter.textView.text.toString())

        // Frame 5: (5 / 5) % 2 == 1 -> Hide cursor
        // At frame 5, visibleCharsCount is 2 (linear interpolation: 5/10 * 5 chars = 2.5 -> 2)
        typeWriter.forFrame(5)
        assertEquals("Hello", typeWriter.textView.text.toString())

        // Frame 10: (10 / 5) % 2 == 0 -> Show cursor
        // At frame 10, visibleCharsCount is 5
        typeWriter.forFrame(10)
        assertEquals("Hello|", typeWriter.textView.text.toString())
        
        // Frame 15: (15 / 5) % 2 == 1 -> Hide cursor
        // Still at the end
        typeWriter.forFrame(15)
        assertEquals("Hello", typeWriter.textView.text.toString())
        
        // Frame 20: (20 / 5) % 2 == 0 -> Show cursor
        typeWriter.forFrame(20)
        assertEquals("Hello|", typeWriter.textView.text.toString())
    }
}
