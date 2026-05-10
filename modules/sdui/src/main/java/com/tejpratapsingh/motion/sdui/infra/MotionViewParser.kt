package com.tejpratapsingh.motion.sdui.infra

import android.content.Context
import android.view.ViewGroup
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.IComposerView

/**
 * Polymorphic serialization for [MotionView].
 */
fun MotionView.toJson(): JsonObject {
    val json = JsonObject()
    json.addProperty("type", this.javaClass.simpleName)
    json.addProperty("startFrame", startFrame)
    json.addProperty("endFrame", endFrame)

    val loopJson = JsonObject()
    loopJson.addProperty("start", loop.first)
    loopJson.addProperty("end", loop.second)
    json.add("loop", loopJson)

    if (effects.isNotEmpty()) {
        val effectsArray = JsonArray()
        effects.forEach { effect ->
            effectsArray.add(effect.toJson())
        }
        json.add("effects", effectsArray)
    }

    if (this is IComposerView) {
        val pluginsArray = JsonArray()
        plugins.forEach { plugin ->
            pluginsArray.add(plugin.toJson())
        }
        if (pluginsArray.size() > 0) {
            json.add("plugins", pluginsArray)
        }
    }

    if (this is ViewGroup) {
        val childrenArray = JsonArray()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is MotionView) {
                childrenArray.add(child.toJson())
            }
        }
        if (childrenArray.size() > 0) {
            json.add("children", childrenArray)
        }
    }

    // Allow concrete implementations to add their own properties
    val serializer = MotionSdui.getViewSerializer(this.javaClass)
    serializer?.serialize(this, json)

    return json
}

/**
 * Polymorphic deserialization for [MotionView].
 */
fun JsonObject.toMotionView(context: Context): MotionView {
    val type = get("type")?.asString ?: throw IllegalArgumentException("Missing 'type' in MotionView JSON")
    val factory = MotionSdui.getViewFactory(type) ?: throw IllegalArgumentException("No factory registered for MotionView type: $type")

    val motionView = factory.create(context, this)

    // Check if the factory already handled effects via parseMotionViewProps or similar.
    // If effects list is empty, try to populate it from JSON.
    if (motionView.effects.isEmpty() && has("effects")) {
        val effectsArray = get("effects").asJsonArray
        effectsArray.forEach { effectJson ->
            if (effectJson is JsonObject) {
                motionView.addEffect(effectJson.toMotionEffect())
            }
        }
    }

    return motionView
}

/**
 * Helper to parse common [MotionView] properties.
 */
fun JsonObject.parseMotionViewProps(): MotionViewProps {
    val startFrame = get("startFrame")?.asInt ?: 0
    val endFrame = get("endFrame")?.asInt ?: 0
    val loop =
        if (has("loop")) {
            val loopObj = get("loop").asJsonObject
            Pair(loopObj.get("start").asInt, loopObj.get("end").asInt)
        } else {
            Pair(0, 0)
        }

    val effects = mutableListOf<MotionEffect>()
    if (has("effects")) {
        val effectsArray = get("effects").asJsonArray
        effectsArray.forEach { effectJson ->
            if (effectJson is JsonObject) {
                effects.add(effectJson.toMotionEffect())
            }
        }
    }

    return MotionViewProps(startFrame, endFrame, loop, effects)
}

data class MotionViewProps(
    val startFrame: Int,
    val endFrame: Int,
    val loop: Pair<Int, Int>,
    val effects: List<MotionEffect>,
)
