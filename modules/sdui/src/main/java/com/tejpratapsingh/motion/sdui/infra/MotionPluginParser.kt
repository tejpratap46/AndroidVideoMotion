package com.tejpratapsingh.motion.sdui.infra

import android.content.Context
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionPlugin

/**
 * Polymorphic serialization for [MotionPlugin].
 */
fun MotionPlugin.toJson(): JsonObject {
    val json = JsonObject()
    json.addProperty("type", this.javaClass.simpleName)

    // Allow concrete implementations to add their own properties
    @Suppress("UNCHECKED_CAST")
    val serializer = MotionSdui.getPluginSerializer(this.javaClass) as? MotionPluginSerializer<MotionPlugin>
    serializer?.serialize(this, json)

    return json
}

/**
 * Polymorphic deserialization for [MotionPlugin].
 */
fun JsonObject.toMotionPlugin(context: Context): MotionPlugin {
    val type = get("type")?.asString ?: throw IllegalArgumentException("Missing 'type' in MotionPlugin JSON")
    val factory = MotionSdui.getPluginFactory(type) ?: throw IllegalArgumentException("No factory registered for MotionPlugin type: $type")

    return factory.create(context, this)
}
