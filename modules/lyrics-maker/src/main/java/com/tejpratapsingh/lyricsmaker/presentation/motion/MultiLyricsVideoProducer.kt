package com.tejpratapsingh.lyricsmaker.presentation.motion

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.lyricsmaker.asLyricsApp
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.view.MultiLyricsContainer
import com.tejpratapsingh.motion.sdui.infra.SDUIMotionVideoProducerFactory
import com.tejpratapsingh.motion.sdui.infra.createMotionSDUIJson
import com.tejpratapsingh.motion.sdui.infra.getMotionAudios
import com.tejpratapsingh.motion.sdui.infra.getMotionConfig
import com.tejpratapsingh.motion.sdui.infra.getMotionPlugins
import com.tejpratapsingh.motion.sdui.infra.getMotionViews
import com.tejpratapsingh.motion.sdui.infra.toMotionView
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.fontSizeH3
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionlib.core.setCurrentConfig
import com.tejpratapsingh.motionlib.ffmpeg.FfmpegVideoProducerAdapter
import com.tejpratapsingh.motionlib.ui.custom.text.PopUpTextView
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
            PopUpTextView(
                context = applicationContext,
                text = current.text,
                startFrame = current.frame,
                endFrame = next.frame,
                writingSpeed = 1.5f,
                unwrittenTextAlpha = 0.3f,
                textView =
                    AppCompatTextView(applicationContext).apply {
                        textSize = fontSizeH3
                        setTextColor(Color.WHITE)
                        setPadding(16, 16, 16, 16)
                        textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                    },
            ).apply {
                contourHeightOf {
                    provideCurrentConfig()
                        .aspectRatio.height
                        .toYInt()
                }
                contourWidthOf {
                    provideCurrentConfig()
                        .aspectRatio.width
                        .toXInt()
                }

                textView.gravity = Gravity.CENTER
            },
        )
    }

    val views = mutableListOf<MotionView>()
    for (i in 0 until producer.motionComposerView.childCount) {
        val child = producer.motionComposerView.getChildAt(i)
        if (child is MotionView) {
            views.add(child)
        }
    }

    val sdui =
        createMotionSDUIJson(
            views = views,
            audios = producer.motionAudio,
            plugins = producer.motionComposerView.plugins,
            config = provideCurrentConfig(),
        )

    motionProject.metadata.add("sdui", sdui)

    val updatedProject = motionProject.copy(sdui = sdui)
    applicationContext.asLyricsApp().motionStoreDao.upsert(updatedProject)

    return producer
}
