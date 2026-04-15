package com.tejpratapsingh.lyricsmaker.presentation.motion

import android.content.Context
import android.util.Log
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.view.LyricsContainer
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.core.setCurrentConfig
import com.tejpratapsingh.motionlib.ffmpeg.FfmpegVideoProducerAdapter

fun getLyricsVideoProducer(
    applicationContext: Context,
    song: String,
    lyrics: List<SyncedLyricFrame>,
    image: String? = null,
): MotionVideoProducer {
    Log.d("MotionVideoProducer", "getLyricsVideoProducer: ${lyrics.size}")

    val motionConfig =
        MotionConfig(
            aspectRatio = VideoAspectRatio.Ratio9x16_480,
            fps = 24,
        )

    setCurrentConfig(motionConfig)

    val motionView =
        LyricsContainer(
            context = applicationContext,
            startFrame = lyrics.first().frame,
            endFrame = lyrics.last().frame,
            songName = song,
            lyrics = lyrics,
            image = image,
        )

    return MotionVideoProducer
        .with(
            context = applicationContext,
            videoProducerAdapter = FfmpegVideoProducerAdapter(),
        ).addMotionViewToSequence(motionView = motionView)
}
