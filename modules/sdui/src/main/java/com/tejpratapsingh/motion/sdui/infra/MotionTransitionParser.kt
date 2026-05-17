package com.tejpratapsingh.motion.sdui.infra

import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionTransition

/**
 * Polymorphic serialization for [MotionTransition].
 */
fun MotionTransition.toJson(): JsonObject {
    val json = JsonObject()
    json.addProperty("type", this.javaClass.simpleName)

    // Allow concrete implementations to add their own properties
    val serializer = MotionSdui.getTransitionSerializer(this.javaClass)
    serializer?.serialize(this, json)

    return json
}

/**
 * Polymorphic deserialization for [MotionTransition].
 */
fun JsonObject.toMotionTransition(): MotionTransition {
    val type = get("type")?.asString ?: throw IllegalArgumentException("Missing 'type' in MotionTransition JSON")
    val factory = MotionSdui.getTransitionFactory(type) ?: throw IllegalArgumentException("No factory registered for MotionTransition type: $type")

    return factory.create(this)
}
