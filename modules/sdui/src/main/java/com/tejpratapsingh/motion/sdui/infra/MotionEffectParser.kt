package com.tejpratapsingh.motion.sdui.infra

import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionEffect

/**
 * Polymorphic serialization for [MotionEffect].
 */
fun MotionEffect.toJson(): JsonObject {
    val json = JsonObject()
    json.addProperty("type", this.javaClass.simpleName)
    json.addProperty("startFrame", startFrame)
    json.addProperty("endFrame", endFrame)

    // Allow concrete implementations to add their own properties
    @Suppress("UNCHECKED_CAST")
    val serializer = MotionSdui.getEffectSerializer(this.javaClass) as? MotionEffectSerializer<MotionEffect>
    serializer?.serialize(this, json)

    return json
}

/**
 * Polymorphic deserialization for [MotionEffect].
 */
fun JsonObject.toMotionEffect(): MotionEffect {
    val type = get("type")?.asString ?: throw IllegalArgumentException("Missing 'type' in MotionEffect JSON")
    val factory = MotionSdui.getEffectFactory(type) ?: throw IllegalArgumentException("No factory registered for MotionEffect type: $type")

    return factory.create(this)
}

/**
 * Helper to parse common [MotionEffect] properties.
 */
fun JsonObject.parseMotionEffectProps(): MotionEffectProps {
    val startFrame = get("startFrame")?.asInt ?: 0
    val endFrame = get("endFrame")?.asInt ?: 0
    return MotionEffectProps(startFrame, endFrame)
}

data class MotionEffectProps(
    val startFrame: Int,
    val endFrame: Int,
)
