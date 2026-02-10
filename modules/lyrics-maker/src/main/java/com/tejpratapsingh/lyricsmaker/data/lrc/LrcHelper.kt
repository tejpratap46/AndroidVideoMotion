package com.tejpratapsingh.lyricsmaker.data.lrc

import android.util.Log

object LrcHelper {
    fun getSyncedLyrics(
        lrcContent: String,
        fps: Int,
    ): List<SyncedLyricFrame> = getSyncedLyricsWithFrameOffset(lrcContent, fps)

    /**
     * Parses the raw LRC string into synced lyric frames.
     * @param fps Frames per second of video
     * @param offsetFrames Offset to shift all lyrics (can be negative)
     * @param parser Custom parser if needed
     */
    fun getSyncedLyricsWithFrameOffset(
        lrcContent: String,
        fps: Int,
        offsetFrames: Int = 0,
        parser: LrcParser = LrcParser(),
    ): List<SyncedLyricFrame> {
        Log.d("getSyncedLyricsWithFrameOffset", "lrcContentString: $lrcContent")
        val parsedResult = parser.parse(lrcContent)

        return parsedResult
            .map {
                val frame =
                    ((it.time / (1000.0 / fps)).toInt() - offsetFrames).coerceAtLeast(0) // avoid negative frames
                Log.d("getSyncedLyricsWithFrameOffset", "(frame,text) - ($frame,${it.text})")
                SyncedLyricFrame(
                    frame = frame,
                    text = it.text,
                )
            }.sortedBy { it.frame }
    }

    /**
     * Same as getSyncedLyrics but offset is in milliseconds.
     * Converts ms to frames before shifting.
     */
    fun getSyncedLyricsWithMsOffset(
        lrcContent: String,
        fps: Int,
        offsetMs: Long = 0L,
    ): List<SyncedLyricFrame> {
        val offsetFrames = (offsetMs / (1000.0 / fps)).toInt()
        return getSyncedLyricsWithFrameOffset(lrcContent, fps, offsetFrames)
    }

    /**
     * Find the current lyric line for a given frame
     */
    fun getCurrentLyric(
        lyrics: List<SyncedLyricFrame>,
        currentFrame: Int,
    ): SyncedLyricFrame? = lyrics.lastOrNull { it.frame <= currentFrame }

    /**
     * Find the next lyric line for a given frame
     */
    fun getNextLyric(
        lyrics: List<SyncedLyricFrame>,
        currentFrame: Int,
    ): SyncedLyricFrame? = lyrics.firstOrNull { it.frame > currentFrame }
}
