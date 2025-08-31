package com.tejpratapsingh.lyricsmaker.data.lrc

class LrcHelper(private val parser: LrcParser = LrcParser()
) {

    /**
     * Parses the raw LRC string into synced lyric frames
     */
    fun getSyncedLyrics(lrcContent: String, fps: Int): List<SyncedLyricFrame> {
        val parsedResult = parser.parse(lrcContent)

        return parsedResult.map {
            val frame = (it.time / (1000.0 / fps)).toInt()  // convert ms → frame
            SyncedLyricFrame(
                frame = frame,
                text = it.text
            )
        }
    }

    /**
     * Find the current lyric line for a given frame
     */
    fun getCurrentLyric(
        lyrics: List<SyncedLyricFrame>,
        currentFrame: Int
    ): SyncedLyricFrame? {
        return lyrics.lastOrNull { it.frame <= currentFrame }
    }

    /**
     * Find the next lyric line for a given frame
     */
    fun getNextLyric(
        lyrics: List<SyncedLyricFrame>,
        currentFrame: Int
    ): SyncedLyricFrame? {
        return lyrics.firstOrNull { it.frame > currentFrame }
    }
}