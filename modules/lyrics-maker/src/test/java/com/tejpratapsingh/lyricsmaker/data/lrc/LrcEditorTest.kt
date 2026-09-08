package com.tejpratapsingh.lyricsmaker.data.lrc

import org.junit.Assert.assertEquals
import org.junit.Test

class LrcEditorTest {
    @Test
    fun `parseTextToLrcEditorLines parses plain text without timestamps`() {
        val input = """
            Line 1
            Line 2
            Line 3
        """.trimIndent()

        val result = parseTextToLrcEditorLines(input)

        assertEquals(3, result.size)
        assertEquals("Line 1", result[0].text)
        assertEquals(0f, result[0].startTime, 0.001f)
        assertEquals(0f, result[0].endTime, 0.001f)

        assertEquals("Line 2", result[1].text)
        assertEquals(0f, result[1].startTime, 0.001f)
        assertEquals(0f, result[1].endTime, 0.001f)
    }

    @Test
    fun `parseTextToLrcEditorLines parses LRC text with timestamps`() {
        val input = """
            [00:05.50] Hello world
            [00:10.00] Second line
            [01:05.25] Third line
        """.trimIndent()

        val result = parseTextToLrcEditorLines(input)

        assertEquals(3, result.size)
        assertEquals("Hello world", result[0].text)
        assertEquals(5.5f, result[0].startTime, 0.001f)
        assertEquals(10.0f, result[0].endTime, 0.001f)

        assertEquals("Second line", result[1].text)
        assertEquals(10.0f, result[1].startTime, 0.001f)
        assertEquals(65.25f, result[1].endTime, 0.001f)

        assertEquals("Third line", result[2].text)
        assertEquals(65.25f, result[2].startTime, 0.001f)
        assertEquals(65.25f, result[2].endTime, 0.001f)
    }

    @Test
    fun `formatSecondsToLrcTimestamp formats seconds accurately`() {
        assertEquals("[00:00.00]", formatSecondsToLrcTimestamp(0f))
        assertEquals("[00:05.50]", formatSecondsToLrcTimestamp(5.5f))
        assertEquals("[01:05.25]", formatSecondsToLrcTimestamp(65.25f))
    }

    @Test
    fun `formatLrcEditorLinesToLrcString converts lines back to standard LRC string`() {
        val lines =
            listOf(
                LrcEditorLine(startTime = 5.5f, text = "Hello", endTime = 10f),
                LrcEditorLine(startTime = 10f, text = "World", endTime = 15f),
            )

        val lrc = formatLrcEditorLinesToLrcString(lines)
        val expected = """
            [00:05.50] Hello
            [00:10.00] World
        """.trimIndent()

        assertEquals(expected, lrc)
    }

    @Test
    fun `auto-placement simulation updates next line start time`() {
        var lines =
            listOf(
                LrcEditorLine(startTime = 0f, text = "First", endTime = 0f),
                LrcEditorLine(startTime = 0f, text = "Second", endTime = 0f),
            )

        // Simulate user updating endTime of line 0 to 5.0s
        val newEnd0 = 5.0f
        val line0Updated = lines[0].copy(endTime = newEnd0)
        val line1Updated =
            lines[1].copy(
                startTime = newEnd0 + 1.0f,
                endTime = maxOf(lines[1].endTime, newEnd0 + 1.0f),
            )

        lines = listOf(line0Updated, line1Updated)

        assertEquals(5.0f, lines[0].endTime, 0.001f)
        assertEquals(6.0f, lines[1].startTime, 0.001f)
        assertEquals(6.0f, lines[1].endTime, 0.001f)
    }

    @Test
    fun `whole number stepping rounds values to integers`() {
        val step = 1.0f
        fun snapToStep(rawVal: Float): Float = kotlin.math.round(rawVal / step) * step

        assertEquals(0f, snapToStep(0.2f), 0.001f)
        assertEquals(1f, snapToStep(0.6f), 0.001f)
        assertEquals(5f, snapToStep(4.8f), 0.001f)
        assertEquals(12f, snapToStep(12.1f), 0.001f)
    }
}
