package com.tejpratapsingh.lyricsmaker.data.lrc

import com.tejpratapsingh.lyricsmaker.domain.TrimLyrics
import com.tejpratapsingh.lyricsmaker.domain.TrimUnit

class LrcHelper(
    private val parser: LrcParser = LrcParser()
) {

    fun getSyncedLyrics(
        lrcContent: String, fps: Int, trimLyrics: TrimLyrics
    ): List<SyncedLyricFrame> {
        return when (trimLyrics.unit) {
            TrimUnit.FRAME -> getSyncedLyricsWithFrameOffset(lrcContent, fps, trimLyrics.start)
            TrimUnit.MILLI_SECOND -> getSyncedLyricsWithMsOffset(
                lrcContent,
                fps,
                trimLyrics.start.toLong()
            )
        }
    }

    /**
     * Parses the raw LRC string into synced lyric frames.
     * @param fps Frames per second of video
     * @param offsetFrames Offset to shift all lyrics (can be negative)
     */
    fun getSyncedLyricsWithFrameOffset(
        lrcContent: String, fps: Int, offsetFrames: Int = 0
    ): List<SyncedLyricFrame> {
        val parsedResult = parser.parse(lrcContent)

        return parsedResult.map {
            val frame =
                ((it.time / (1000.0 / fps)).toInt() - offsetFrames).coerceAtLeast(0) // avoid negative frames
            SyncedLyricFrame(
                frame = frame, text = it.text
            )
        }.sortedBy { it.frame }
    }

    /**
     * Same as getSyncedLyrics but offset is in milliseconds.
     * Converts ms to frames before shifting.
     */
    fun getSyncedLyricsWithMsOffset(
        lrcContent: String, fps: Int, offsetMs: Long = 0L
    ): List<SyncedLyricFrame> {
        val offsetFrames = (offsetMs / (1000.0 / fps)).toInt()
        return getSyncedLyricsWithFrameOffset(lrcContent, fps, offsetFrames)
    }

    /**
     * Find the current lyric line for a given frame
     */
    fun getCurrentLyric(
        lyrics: List<SyncedLyricFrame>, currentFrame: Int
    ): SyncedLyricFrame? {
        return lyrics.lastOrNull { it.frame <= currentFrame }
    }

    /**
     * Find the next lyric line for a given frame
     */
    fun getNextLyric(
        lyrics: List<SyncedLyricFrame>, currentFrame: Int
    ): SyncedLyricFrame? {
        return lyrics.firstOrNull { it.frame > currentFrame }
    }
}