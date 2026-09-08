package com.tejpratapsingh.lyricsmaker.data.lrc

import java.util.Locale
import java.util.UUID

/**
 * Data model for a single line in the LRC Editor.
 *
 * @property id Unique identifier for Compose keying.
 * @property startTime Start time in seconds.
 * @property text Lyrics text line.
 * @property endTime End time in seconds.
 */
data class LrcEditorLine(
    val id: String = UUID.randomUUID().toString(),
    val startTime: Float,
    val text: String,
    val endTime: Float,
)

private val LRC_TIMESTAMP_REGEX = Regex("""\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?]""")

/**
 * Parses raw text into a list of [LrcEditorLine]s.
 *
 * If the input string contains LRC timestamps (e.g., `[00:05.00] lyric`), those timestamps
 * are parsed into [LrcEditorLine.startTime] and [LrcEditorLine.endTime].
 * If no timestamps are present, every line defaults to startTime = 0.0s and endTime = 0.0s.
 */
fun parseTextToLrcEditorLines(rawText: String): List<LrcEditorLine> {
    val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
    if (lines.isEmpty()) return emptyList()

    val hasTimestamps = lines.any { LRC_TIMESTAMP_REGEX.containsMatchIn(it) }

    return if (hasTimestamps) {
        val parsedEntries = mutableListOf<Pair<Float, String>>()

        lines.forEach { line ->
            val match = LRC_TIMESTAMP_REGEX.find(line)
            if (match != null) {
                val min = match.groupValues[1].toIntOrNull() ?: 0
                val sec = match.groupValues[2].toIntOrNull() ?: 0
                val msStr = match.groupValues.getOrNull(3)?.padEnd(3, '0') ?: "0"
                val ms = msStr.toIntOrNull() ?: 0

                val timeInSeconds = (min * 60f) + sec + (ms / 1000f)
                val lyricText = line.replace(LRC_TIMESTAMP_REGEX, "").trim()
                parsedEntries.add(Pair(timeInSeconds, lyricText))
            } else {
                parsedEntries.add(Pair(0f, line))
            }
        }

        parsedEntries.mapIndexed { index, (startTime, text) ->
            val endTime =
                if (index < parsedEntries.size - 1) {
                    parsedEntries[index + 1].first
                } else {
                    startTime
                }
            LrcEditorLine(
                startTime = startTime,
                text = text,
                endTime = endTime,
            )
        }
    } else {
        lines.map { line ->
            LrcEditorLine(
                startTime = 0f,
                text = line,
                endTime = 0f,
            )
        }
    }
}

/**
 * Formats a list of [LrcEditorLine]s into standard LRC format.
 */
fun formatLrcEditorLinesToLrcString(lines: List<LrcEditorLine>): String {
    val sb = StringBuilder()
    lines.forEach { line ->
        val timestamp = formatSecondsToLrcTimestamp(line.startTime)
        sb.append("$timestamp ${line.text}\n")
    }
    return sb.toString().trimEnd()
}

/**
 * Helper to convert seconds into `[MM:SS.ss]` timestamp string.
 */
fun formatSecondsToLrcTimestamp(seconds: Float): String {
    val totalMs = (seconds * 1000f).toLong().coerceAtLeast(0L)
    val minutes = totalMs / (60 * 1000)
    val secs = (totalMs % (60 * 1000)) / 1000
    val hundredths = (totalMs % 1000) / 10

    return String.format(Locale.US, "[%02d:%02d.%02d]", minutes, secs, hundredths)
}
