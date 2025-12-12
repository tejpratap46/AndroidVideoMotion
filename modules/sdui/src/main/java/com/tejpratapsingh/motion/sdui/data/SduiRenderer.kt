package com.tejpratapsingh.motion.sdui.data

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.domain.ActionHandler
import com.tejpratapsingh.motion.sdui.domain.ImageLoader
import com.tejpratapsingh.motion.sdui.domain.ViewFactory
import com.tejpratapsingh.motion.sdui.presentation.ContainerFactory
import com.tejpratapsingh.motion.sdui.presentation.DefaultActionHandler
import com.tejpratapsingh.motion.sdui.presentation.ImageFactory
import com.tejpratapsingh.motion.sdui.presentation.TextFactory

class SduiRenderer(
    val actionHandler: ActionHandler = DefaultActionHandler(),
    val imageLoader: ImageLoader? = null,
) {
    private val gson = Gson()
    private val factories: MutableMap<String, ViewFactory> = mutableMapOf()

    init {
        register("container", ContainerFactory())
        register("text", TextFactory())
        register("image", ImageFactory())
    }

    fun register(
        type: String,
        factory: ViewFactory,
    ) {
        factories[type] = factory
    }

    fun createView(
        context: Context,
        json: JsonObject,
    ): View {
        val type = json.get("type")?.asString ?: error("Missing type")
        val factory = factories[type] ?: throw IllegalArgumentException("No factory for type $type")
        return factory.create(context, json, this)
    }

    fun renderInto(
        container: ViewGroup,
        json: String,
    ) {
        val rootJson = gson.fromJson(json, JsonObject::class.java)
        val view = createView(container.context, rootJson)
        container.addView(view)
    }
}
