package com.tejpratapsingh.motion.sdui.infra

import android.content.Context
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionAsset

/**
 * Utility to extract all [MotionAsset]s from SDUI JSON.
 */
object MotionAssetExtractor {

    /**
     * Recursively extracts all [MotionAsset]s from the given [json].
     */
    fun extractAssets(context: Context, json: JsonElement): List<MotionAsset> {
        val assets = mutableListOf<MotionAsset>()
        
        when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                
                // If it's a MotionAsset (or has type that we can parse as one)
                // We can try to use toMotionAsset if it has a 'type' property
                if (obj.has("type") && isAssetType(obj.get("type").asString)) {
                    try {
                        assets.add(obj.toMotionAsset(context))
                    } catch (_: Exception) {
                        // Not a valid asset or failed to parse, continue search
                    }
                } else if (obj.has("asset") && obj.get("asset").isJsonObject) {
                   // Some views have an "asset" property directly
                   try {
                       assets.add(obj.get("asset").asJsonObject.toMotionAsset(context))
                   } catch (_: Exception) {
                       // Continue
                   }
                }
                
                // Recursively search all properties
                obj.entrySet().forEach { (_, value) ->
                    assets.addAll(extractAssets(context, value))
                }
            }
            json.isJsonArray -> {
                json.asJsonArray.forEach { element ->
                    assets.addAll(extractAssets(context, element))
                }
            }
        }
        
        return assets.distinctBy { it.getUri().toString() }
    }
    
    private fun isAssetType(type: String): Boolean {
        return type.endsWith("Asset")
    }
}
