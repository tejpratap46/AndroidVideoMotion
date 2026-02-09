package com.tejpratapsingh.motion.sdui.infra

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionConfig

fun MotionConfig.toJsonObject(): JsonObject =
    Gson()
        .toJsonTree(
            mapOf<String, Any>(
                "aspectRatio" to aspectRatio.toJsonObject(),
                "fps" to fps,
                "outputQuality" to outputQuality,
            ),
        ).asJsonObject

fun JsonObject.toMotionConfig(): MotionConfig =
    MotionConfig(
        aspectRatio = get("aspectRatio").asJsonObject.toVideoAspectRatio(),
        fps = get("fps").asInt,
        outputQuality = get("outputQuality").asInt,
    )
