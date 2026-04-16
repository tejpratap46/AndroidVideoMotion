package com.tejpratapsingh.lyricsmaker.presentation.motion

import android.content.Context
import timber.log.Timber
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.view.LyricsContainer
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionlib.core.setCurrentConfig
import com.tejpratapsingh.motionlib.ffmpeg.FfmpegVideoProducerAdapter
import com.tejpratapsingh.motionstore.tables.MotionProject

fun getLyricsVideoProducer(
    applicationContext: Context,
    motionProject: MotionProject,
): MotionVideoProducer {
    val song = motionProject.name
    val image = motionProject.metadata.get("image")?.takeIf { it.isJsonPrimitive }?.asString
    val lyrics =
        motionProject.metadata.get("lyrics")?.takeIf { it.isJsonArray }?.asJsonArray?.map {
            SyncedLyricFrame(
                frame = it.asJsonObject.get("frame")?.takeIf { f -> f.isJsonPrimitive }?.asInt ?: 0,
                text = it.asJsonObject.get("text")?.takeIf { t -> t.isJsonPrimitive }?.asString ?: "",
            )
        } ?: emptyList()

    Timber.d("getLyricsVideoProducer: lyrics size = ${lyrics.size}")

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
