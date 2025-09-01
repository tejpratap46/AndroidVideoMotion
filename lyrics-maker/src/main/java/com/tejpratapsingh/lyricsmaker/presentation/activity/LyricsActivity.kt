package com.tejpratapsingh.lyricsmaker.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.tejpratapsingh.lyricsmaker.data.api.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.presentation.motion.getLyricsVideoProducer
import com.tejpratapsingh.lyricsmaker.presentation.worker.LyricsMotionWorker
import com.tejpratapsingh.motionlib.activities.PreviewActivity
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer

class LyricsActivity : PreviewActivity() {

    companion object {
        private const val TAG = "LyricsActivity"

        private const val LYRICS = "lyrics"

        fun start(context: Context, lyrics: LyricsResponse) {
            context.startActivity(
                Intent(context, LyricsActivity::class.java).also {
                    it.putExtra(LYRICS, lyrics)
                })
        }
    }

    private val lyrics by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(LYRICS, LyricsResponse::class.java)
        } else {
            intent.getParcelableExtra(LYRICS)
        }
    }

    private val video by lazy {
        getLyricsVideoProducer(applicationContext, lyrics!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LyricsMotionWorker.startWork(applicationContext, lyrics!!)
    }

    override fun getMotionVideo(): MotionVideoProducer {
        return video
    }
}