package com.tejpratapsingh.lyricsmaker.presentation.motion

import android.content.Context
import com.tejpratapsingh.lyricsmaker.presentation.view.LyricsContainer
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.motion.BaseMotionView
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer

fun getLyricsVideoProducer(
    applicationContext: Context,
    songName: String,
    lyrics: String
): MotionVideoProducer {
    val motionConfig = MotionConfig(
        width = 768, height = 1366, fps = 30
    )

    val motionView: BaseMotionView = LyricsContainer(
        context = applicationContext,
        startFrame = 1,
        endFrame = motionConfig.fps * 30,
        songName = songName,
        lyrics = lyrics
    )

    return MotionVideoProducer.with(
        context = applicationContext,
        config = motionConfig,
    ).addMotionViewToSequence(motionView = motionView)
}