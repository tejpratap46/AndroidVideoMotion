package com.tejpratapsingh.lyricsmaker.presentation.motion

import android.content.Context
import android.util.Log
import com.tejpratapsingh.lyricsmaker.data.api.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.presentation.view.LyricsContainer
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.motion.BaseMotionView
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.ffmpeg.FfmpegVideoProducerAdapter

fun getLyricsVideoProducer(
    applicationContext: Context, lyrics: LyricsResponse
): MotionVideoProducer {

    Log.d("MotionVideoProducer", "getLyricsVideoProducer: ${lyrics.trackName}")
    Log.d("MotionVideoProducer", "getLyricsVideoProducer: ${lyrics.getLyrics()}")

    val motionConfig = MotionConfig(
        width = 768, height = 1366, fps = 24
    )

    val motionView: BaseMotionView = LyricsContainer(
        context = applicationContext,
        startFrame = 1,
        endFrame = motionConfig.fps * (lyrics.duration?.toInt() ?: 10),
        songName = lyrics.trackName,
        lyrics = lyrics.getLyrics()
    )

    return MotionVideoProducer.with(
        context = applicationContext,
        config = motionConfig,
        videoProducerAdapter = FfmpegVideoProducerAdapter()
    ).addMotionViewToSequence(motionView = motionView)
}