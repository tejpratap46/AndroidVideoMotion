package com.tejpratapsingh.lyricsmaker.presentation.motion

import android.content.Context
import com.tejpratapsingh.lyricsmaker.asLyricsApp
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.templates.LyricsTemplateRegistry
import com.tejpratapsingh.motion.sdui.infra.createMotionSDUIJson
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionlib.core.setCurrentConfig
import com.tejpratapsingh.motionlib.ffmpeg.FfmpegVideoProducerAdapter
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate
import com.tejpratapsingh.motionlib.templates.model.TemplateData
import com.tejpratapsingh.motionstore.tables.MotionProject
import timber.log.Timber

fun extractLyricsTemplateData(motionProject: MotionProject): TemplateData {
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
            )
        } ?: emptyList()

    val image =
        motionProject.metadata
            .get("image")
            ?.takeIf { it.isJsonPrimitive }
            ?.asString

    return TemplateData(
        mapOf(
            "songName" to motionProject.name,
            "image" to (image ?: ""),
            "lyrics" to lyrics,
        ),
    )
}

fun getLyricsVideoProducer(
    applicationContext: Context,
    motionProject: MotionProject,
): MotionVideoProducer {
    val producer =
        createLyricsVideoProducer(
            applicationContext = applicationContext,
            motionProject = motionProject,
        )

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

fun createLyricsVideoProducer(
    applicationContext: Context,
    motionProject: MotionProject,
    templateOverride: MotionTemplate? = null,
): MotionVideoProducer =
    createLyricsVideoProducerInternal(
        applicationContext = applicationContext,
        motionProject = motionProject,
        templateOverride = templateOverride,
        isPreview = false,
    )

fun createLyricsVideoPreviewProducer(
    applicationContext: Context,
    motionProject: MotionProject,
    templateOverride: MotionTemplate? = null,
): MotionVideoProducer =
    createLyricsVideoProducerInternal(
        applicationContext = applicationContext,
        motionProject = motionProject,
        templateOverride = templateOverride,
        isPreview = true,
    )

private fun createLyricsVideoProducerInternal(
    applicationContext: Context,
    motionProject: MotionProject,
    templateOverride: MotionTemplate? = null,
    isPreview: Boolean = false,
): MotionVideoProducer {
    Timber.i("createLyricsVideoProducerInternal: $motionProject, isPreview: $isPreview")

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
            )

    val templateData = extractLyricsTemplateData(motionProject)

    val contentScope =
        ContentScope(
            context = applicationContext,
            producer = producer,
            data = templateData,
        )

    val templateName =
        motionProject.metadata
            .get("template")
            ?.takeIf { it.isJsonPrimitive }
            ?.asString
    val template = templateOverride ?: LyricsTemplateRegistry.getTemplate(templateName)

    if (isPreview) {
        template.buildPreview(contentScope)
    } else {
        template.buildContent(contentScope)
    }

    producer.motionComposerView.forFrame(0)

    return producer
}
