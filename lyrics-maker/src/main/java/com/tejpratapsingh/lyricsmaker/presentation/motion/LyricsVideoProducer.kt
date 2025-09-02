package com.tejpratapsingh.lyricsmaker.presentation.motion

import android.content.Context
import android.util.Log
import com.tejpratapsingh.lyricsmaker.data.api.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.domain.TrimLyrics
import com.tejpratapsingh.lyricsmaker.presentation.view.LyricsContainer
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.motion.BaseMotionView
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.ffmpeg.FfmpegVideoProducerAdapter
import kotlin.math.min

fun getLyricsVideoProducer(
    applicationContext: Context, lyrics: LyricsResponse, trimLyrics: TrimLyrics
): MotionVideoProducer {

    Log.d("MotionVideoProducer", "getLyricsVideoProducer: ${lyrics.trackName}")
    Log.d("MotionVideoProducer", "getLyricsVideoProducer: ${lyrics.getLyrics()}")

    val motionConfig = MotionConfig(
        aspectRatio = VideoAspectRatio.Ratio9x16_480, fps = 24
    )

    val motionView: BaseMotionView = LyricsContainer(
        context = applicationContext,
        startFrame = 1,
        endFrame = min(
            motionConfig.fps * (lyrics.duration?.toInt() ?: 10),
            trimLyrics.getEndFrame(motionConfig.fps)
        ),
        songName = lyrics.trackName,
        lyrics = lyrics.getLyrics(),
        trimLyrics = trimLyrics
    )

    return MotionVideoProducer.with(
        context = applicationContext,
        config = motionConfig,
        videoProducerAdapter = FfmpegVideoProducerAdapter()
    ).addMotionViewToSequence(motionView = motionView)
}