package com.tejpratapsingh.lyricsmaker.presentation.motion

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.view.LyricsContainer
import com.tejpratapsingh.lyricsmaker.presentation.view.MultiLyricsContainer
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.core.setCurrentConfig
import com.tejpratapsingh.motionlib.ffmpeg.FfmpegVideoProducerAdapter
import com.tejpratapsingh.motionlib.ui.custom.text.TypeWriterTextView
import com.tejpratapsingh.motionlib.ui.custom.text.WordWriterTextView
import com.tejpratapsingh.motionstore.tables.MotionProject
import timber.log.Timber

fun getMultiLyricsVideoProducer(
    applicationContext: Context,
    motionProject: MotionProject,
): MotionVideoProducer {
    Timber.i("getLyricsVideoProducer: $motionProject")

    val lyrics =
        motionProject.metadata.get("lyrics")?.takeIf { it.isJsonArray }?.asJsonArray?.map {
            SyncedLyricFrame(
                frame =
                    it.asJsonObject
                        .get("frame")
                        ?.takeIf { f -> f.isJsonPrimitive }
                        ?.asInt ?: 0,
                text =
                    it.asJsonObject
                        .get("text")
                        ?.takeIf { t -> t.isJsonPrimitive }
                        ?.asString ?: "",
            ).also { lyricFrame ->
                Timber.d("lyricFrame: $lyricFrame")
            }
        } ?: emptyList()

    Timber.d("getMultiLyricsVideoProducer: ${lyrics.size}")

    val image =
        motionProject.metadata
            .get("image")
            ?.takeIf { it.isJsonPrimitive }
            ?.asString

    val motionConfig =
        MotionConfig(
            aspectRatio = VideoAspectRatio.Ratio9x16_480,
            fps = 24,
        )

    setCurrentConfig(motionConfig)

    val producer =
        MotionVideoProducer
            .with(
                context = applicationContext,
                videoProducerAdapter = FfmpegVideoProducerAdapter(),
            ).addMotionViewToSequence(
                MultiLyricsContainer(
                    context = applicationContext,
                    startFrame = lyrics.first().frame,
                    endFrame = lyrics.last().frame,
                    songName = motionProject.name,
                    image = image,
                ),
            )

    lyrics.zipWithNext().forEach { (current, next) ->
        producer.addMotionViewToSequence(
            WordWriterTextView(
                context = applicationContext,
                text = current.text,
                startFrame = current.frame,
                endFrame = next.frame,
                writingSpeed = 1.5f,
                unwrittenTextAlpha = 0.3f,
                textView =
                    AppCompatTextView(applicationContext).apply {
                        textSize = 18f
                        setTextColor(Color.WHITE)
                        setPadding(16, 16, 16, 16)
                        textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                    },
            ),
        )
    }

    return producer
}
