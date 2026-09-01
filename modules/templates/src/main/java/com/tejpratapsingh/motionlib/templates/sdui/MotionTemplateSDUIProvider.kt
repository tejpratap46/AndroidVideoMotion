package com.tejpratapsingh.motionlib.templates.sdui

import android.content.Context
import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.infra.createMotionSDUIJson
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate
import com.tejpratapsingh.motionlib.templates.model.TemplateData

/**
 * Provider to generate SDUI JSON from [MotionTemplate].
 */
object MotionTemplateSDUIProvider {
    /**
     * Generate SDUI JSON from [MotionTemplate].
     *
     * @param context [Context] to use for building template.
     * @param template [MotionTemplate] to generate SDUI from.
     * @param data [TemplateData] to apply to template.
     * @param config [MotionConfig] to use for template. If null, current config will be used.
     * @return [JsonObject] representing SDUI.
     */
    fun provideSDUI(
        context: Context,
        template: MotionTemplate,
        data: TemplateData,
        config: MotionConfig? = null,
    ): JsonObject {
        val motionConfig = config ?: MotionConfig()

        val producer =
            MotionVideoProducer.with(
                context = context,
                motionConfig = motionConfig,
            )

        val contentScope =
            ContentScope(
                context = context,
                producer = producer,
                data = data,
            )

        template.buildContent(contentScope)

        val views = mutableListOf<MotionView>()
        for (i in 0 until producer.motionComposerView.childCount) {
            val child = producer.motionComposerView.getChildAt(i)
            if (child is MotionView) {
                views.add(child)
            }
        }

        return createMotionSDUIJson(
            views = views,
            audios = producer.motionAudio,
            plugins = producer.motionComposerView.plugins,
            config = motionConfig,
        )
    }
}
