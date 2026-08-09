package com.tejpratapsingh.motion.sdui.infra

import android.content.Context
import androidx.core.net.toUri
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.assettype.SimpleMotionAsset
import com.tejpratapsingh.motionlib.core.MotionAsset

/**
 * Polymorphic serialization for [MotionAsset].
 */
fun MotionAsset.toJson(): JsonObject {
    val json = JsonObject()
    json.addProperty("type", this.javaClass.simpleName)
    json.addProperty("uri", getUri().toString())
    getMetadata()?.let { json.add("metadata", it) }

    // Allow concrete implementations to add their own properties
    @Suppress("UNCHECKED_CAST")
    val serializer = MotionSdui.getAssetSerializer(this.javaClass)
    serializer?.serialize(this, json)

    return json
}

/**
 * Polymorphic deserialization for [MotionAsset].
 */
fun JsonObject.toMotionAsset(context: Context): MotionAsset {
    val type = get("type")?.asString ?: SimpleMotionAsset::class.java.simpleName
    val factory = MotionSdui.getAssetFactory(type)

    return if (factory != null) {
        factory.create(context, this)
    } else {
        // Default deserialization for standard SimpleMotionAsset
        val uriString = get("uri")?.asString ?: throw IllegalArgumentException("Missing 'uri' in MotionAsset JSON")
        val metadata = get("metadata")?.asJsonObject
        SimpleMotionAsset(uriString.toUri(), metadata)
    }
}
