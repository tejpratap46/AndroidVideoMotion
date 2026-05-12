package com.tejpratapsingh.motion.sdui.infra

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.core.MotionView

/**
 * Create [JsonObject] for SDUI.
 */
fun createMotionSDUIJson(
    views: List<MotionView> = emptyList(),
    audios: List<MotionAudio> = emptyList(),
    plugins: List<MotionPlugin> = emptyList(),
    config: MotionConfig? = null,
): JsonObject {
    val json = JsonObject()

    val viewsArray = JsonArray()
    views.forEach { view ->
        viewsArray.add(view.toJson())
    }
    json.add("views", viewsArray)

    val audiosArray = JsonArray()
    audios.forEach { audio ->
        audiosArray.add(audio.toJson())
    }
    json.add("audios", audiosArray)

    val pluginsArray = JsonArray()
    plugins.forEach { plugin ->
        pluginsArray.add(plugin.toJson())
    }
    json.add("plugins", pluginsArray)

    config?.let {
        json.add("config", it.toJson())
    }

    return json
}

/**
 * Get [MotionView]s from [JsonObject].
 */
fun JsonObject.getMotionViews(context: Context): List<MotionView> {
    val views = mutableListOf<MotionView>()
    if (has("views")) {
        val viewsArray = get("views").asJsonArray
        viewsArray.forEach { viewElement ->
            views.add(viewElement.asJsonObject.toMotionView(context))
        }
    }
    return views
}

/**
 * Get [MotionAudio]s from [JsonObject].
 */
fun JsonObject.getMotionAudios(context: Context): List<MotionAudio> {
    val audios = mutableListOf<MotionAudio>()
    if (has("audios")) {
        val audiosArray = get("audios").asJsonArray
        audiosArray.forEach { audioElement ->
            audios.add(audioElement.asJsonObject.toMotionAudio(context))
        }
    }
    return audios
}

/**
 * Get [MotionPlugin]s from [JsonObject].
 */
fun JsonObject.getMotionPlugins(context: Context): List<MotionPlugin> {
    val plugins = mutableListOf<MotionPlugin>()
    if (has("plugins")) {
        val pluginsArray = get("plugins").asJsonArray
        pluginsArray.forEach { pluginElement ->
            plugins.add(pluginElement.asJsonObject.toMotionPlugin(context))
        }
    }
    return plugins
}

/**
 * Get [MotionConfig] from [JsonObject].
 */
fun JsonObject.getMotionConfig(): MotionConfig? {
    return if (has("config") && get("config").isJsonObject) {
        get("config").asJsonObject.toMotionConfig()
    } else {
        null
    }
}
