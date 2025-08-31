package com.tejpratapsingh.lyricsmaker.presentation.activity

import android.os.Bundle
import android.util.Log
import com.tejpratapsingh.lyricsmaker.data.api.client.LrcLibClient
import com.tejpratapsingh.lyricsmaker.data.api.model.SearchQuery
import com.tejpratapsingh.lyricsmaker.presentation.motion.getLyricsVideoProducer
import com.tejpratapsingh.motionlib.activities.PreviewActivity
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import kotlinx.coroutines.runBlocking

class LyricsActivity : PreviewActivity() {

    companion object {
        private const val TAG = "LyricsActivity"
    }

    private val video by lazy {
        runBlocking {
            val client = LrcLibClient()
            val lyric = client.searchLyrics(
                SearchQuery(
                    "saiyaara - tanishk bagchi"
                )
            )
            lyric.firstOrNull()?.syncedLyrics?.let { lrc ->
                Log.i(TAG, "onCreate: Lyrics")
                Log.i(TAG, "onCreate: $lrc")

                getLyricsVideoProducer(applicationContext, "Saiyaara", lrc)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun getMotionVideo(): MotionVideoProducer {
        return video ?: getLyricsVideoProducer(
            applicationContext,
            "Saiyaara",
            "[00:00.00] Not Found"
        )
    }
}