package com.tejpratapsingh.lyricsmaker.data.lrc

class LrcParser {
    private val regex = Regex("""\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?]""")

    fun parse(lrc: String): List<LrcLine> {
        val lines = mutableListOf<LrcLine>()

        lrc.lines().forEach { rawLine ->
            val matches = regex.findAll(rawLine)
            val lyricText = rawLine.replace(regex, "").trim()

            matches.forEach { match ->
                val min = match.groupValues[1].toInt()
                val sec = match.groupValues[2].toInt()
                val ms =
                    match.groupValues
                        .getOrNull(3)
                        ?.padEnd(3, '0')
                        ?.toIntOrNull() ?: 0

                val timeMs = (min * 60 * 1000 + sec * 1000 + ms).toLong()
                lines.add(LrcLine(timeMs, lyricText))
            }
        }

        return lines.sortedBy { it.time }
    }
}
