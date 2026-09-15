package com.tejpratapsingh.motion.sdui.infra

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionConfig

private val gson = Gson()

fun MotionConfig.toJson(): JsonObject =
    gson
        .toJsonTree(
            mapOf(
                "aspectRatio" to aspectRatio.toJson(),
                "fps" to fps,
                "outputQuality" to outputQuality,
            ),
        ).asJsonObject

fun JsonObject.toMotionConfig(): MotionConfig {
    val aspectRatio =
        if (has("aspectRatio") && get("aspectRatio").isJsonObject) {
            get("aspectRatio").asJsonObject.toVideoAspectRatio()
        } else {
            MotionConfig().aspectRatio
        }

    return MotionConfig(
        aspectRatio = aspectRatio,
        fps = if (has("fps")) get("fps").asInt else 24,
        outputQuality = if (has("outputQuality")) get("outputQuality").asInt else 100,
    )
}

fun JsonObject.updateMotionConfig(newConfig: MotionConfig) {
    add("config", newConfig.toJson())
}
