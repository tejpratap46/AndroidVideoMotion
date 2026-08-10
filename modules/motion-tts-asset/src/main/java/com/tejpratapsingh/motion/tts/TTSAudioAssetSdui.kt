package com.tejpratapsingh.motion.tts

import android.content.Context
import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.infra.MotionAssetFactory
import com.tejpratapsingh.motion.sdui.infra.MotionAssetSerializer
import com.tejpratapsingh.motion.sdui.infra.MotionSdui
import com.tejpratapsingh.motionlib.core.MotionAsset

/**
 * SDUI components for [TTSAudioAsset].
 */
object TTSAudioAssetSdui {
    /**
     * Register [TTSAudioAsset] for SDUI serialization/deserialization.
     */
    fun register() {
        MotionSdui.registerAsset("TTSAudioAsset", TTSAudioAssetFactory())
        MotionSdui.registerAssetSerializer(TTSAudioAsset::class.java, TTSAudioAssetSerializer())
    }
}

class TTSAudioAssetFactory : MotionAssetFactory {
    override fun create(context: Context, json: JsonObject): MotionAsset {
        val metadata = json.get("metadata")?.asJsonObject
        val text = metadata?.get("text")?.asString ?: ""
        return TTSAudioAsset(text, metadata)
    }
}

class TTSAudioAssetSerializer : MotionAssetSerializer<TTSAudioAsset> {
    override fun serialize(asset: TTSAudioAsset, json: JsonObject) {
        // Basic properties like type, uri, metadata are already handled by MotionAsset.toJson()
    }
}
