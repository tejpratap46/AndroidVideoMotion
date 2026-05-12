package com.tejpratapsingh.motion.sdui.infra

import android.content.Context
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionAudio
import java.io.File

/**
 * Polymorphic serialization for [MotionAudio].
 */
fun MotionAudio.toJson(): JsonObject {
    val json = JsonObject()
    json.addProperty("type", this.javaClass.simpleName)
    json.addProperty("file", file.absolutePath)
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
        val filePath = get("file")?.asString ?: throw IllegalArgumentException("Missing 'file' in MotionAudio JSON")
        val startFrame = get("startFrame")?.asInt ?: 0
        val endFrame = get("endFrame")?.asInt ?: 0
        val delayFrame = get("delayFrame")?.asInt ?: 0

        MotionAudio(
            file = File(filePath),
            startFrame = startFrame,
            endFrame = endFrame,
            delayFrame = delayFrame,
        )
    }
}
