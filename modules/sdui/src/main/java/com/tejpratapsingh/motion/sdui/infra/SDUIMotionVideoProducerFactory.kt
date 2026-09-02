package com.tejpratapsingh.motion.sdui.infra

import android.content.Context
import android.view.ViewGroup
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.core.adapter.AndroidVideoProducerAdapter
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionstore.tables.MotionProject
import timber.log.Timber

/**
 * Factory to create [MotionVideoProducer] from [MotionProject] or SDUI [JsonObject].
 */
class SDUIMotionVideoProducerFactory(
    private val context: Context,
    private val videoProducerAdapter: VideoProducerAdapter = AndroidVideoProducerAdapter(),
) {
    /**
     * Create [MotionVideoProducer] from [MotionProject].
     */
    fun createFromProject(
        project: MotionProject,
        onViewsCreated: ((List<MotionView>) -> Unit)? = null,
    ): MotionVideoProducer = createFromSdui(project.sdui, onViewsCreated)

    /**
     * Create [MotionVideoProducer] from SDUI [JsonObject].
     */
    fun createFromSdui(
        sdui: JsonObject,
        onViewsCreated: ((List<MotionView>) -> Unit)? = null,
    ): MotionVideoProducer {
        val config = sdui.getMotionConfig() ?: com.tejpratapsingh.motionlib.core.MotionConfig()

        val plugins = sdui.getMotionPlugins(context)
        val audios = sdui.getMotionAudios(context)
        val views = sdui.getMotionViews(context)

        onViewsCreated?.invoke(views)

        val producer =
            MotionVideoProducer.with(
                context = context,
                motionConfig = config,
                plugins = plugins,
                motionAudio = audios,
                videoProducerAdapter = videoProducerAdapter,
            )

        if (sdui.has("sequence")) {
            val sequenceArray = sdui.get("sequence").asJsonArray
            sequenceArray.forEach { item ->
                val json = item.asJsonObject
                val type = json.get("type")?.asString
                if (type != null) {
                    val viewFactory = MotionSdui.getViewFactory(type)
                    if (viewFactory != null) {
                        val view = json.toMotionView(context)
                        if (view is ViewGroup) {
                            producer.addMotionViewToSequence(view)
                        }
                    } else {
                        val transitionFactory = MotionSdui.getTransitionFactory(type)
                        if (transitionFactory != null) {
                            val transition = json.toMotionTransition()
                            val duration = json.get("duration")?.asInt ?: 30
                            producer.addTransition(transition, duration)
                        }
                    }
                }
            }
        } else {
            views.forEach { view ->
                if (view is ViewGroup) {
                    // We use a helper to safely call addMotionViewToSequence which has multiple bounds
                    Timber.i("view is supported: ${view::class.java.simpleName}")
                    producer.addMotionViewToSequence(view)
                } else {
                    Timber.w("view not supported: ${view::class.java.simpleName}")
                }
            }
        }

        return producer
    }
}
