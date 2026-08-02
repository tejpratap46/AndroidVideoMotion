package com.tejpratapsingh.motion.sdui.infra

import android.content.Context
import android.net.Uri
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionAudio

/**
 * Polymorphic serialization for [MotionAudio].
 */
fun MotionAudio.toJson(): JsonObject {
    val json = JsonObject()
    json.addProperty("type", this.javaClass.simpleName)
    json.addProperty("audioUri", audioUri.toString())
    json.addProperty("startFrame", startFrame)
    json.addProperty("endFrame", endFrame)
    json.addProperty("delayFrame", delayFrame)

    // Allow concrete implementations (if any) to add their own properties
    @Suppress("UNCHECKED_CAST")
    val serializer = MotionSdui.getAudioSerializer(this.javaClass) as? MotionAudioSerializer<MotionAudio>
    serializer?.serialize(this, json)

    return json
}

/**
 * Polymorphic deserialization for [MotionAudio].
 */
fun JsonObject.toMotionAudio(context: Context): MotionAudio {
    val type = get("type")?.asString ?: MotionAudio::class.java.simpleName // Default to MotionAudio if type is missing
    val factory = MotionSdui.getAudioFactory(type)

    return if (factory != null) {
        factory.create(context, this)
    } else {
        // Default deserialization for standard MotionAudio
        val uriString = get("audioUri")?.asString ?: get("file")?.asString ?: throw IllegalArgumentException("Missing 'audioUri' in MotionAudio JSON")
        val startFrame = get("startFrame")?.asInt ?: 0
        val endFrame = get("endFrame")?.asInt ?: 0
        val delayFrame = get("delayFrame")?.asInt ?: 0

        MotionAudio(
            audioUri = Uri.parse(uriString),
            startFrame = startFrame,
            endFrame = endFrame,
            delayFrame = delayFrame,
        )
    }
}
