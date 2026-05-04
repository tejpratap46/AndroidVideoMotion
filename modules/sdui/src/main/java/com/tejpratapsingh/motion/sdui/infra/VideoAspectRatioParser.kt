package com.tejpratapsingh.motion.sdui.infra

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Custom
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio16x9_1080
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio16x9_1440
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio16x9_2160
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio16x9_480
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio16x9_720
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio1x1_1080
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio1x1_480
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio1x1_720
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio21x9_1080
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio21x9_2160
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio4x3_480
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio4x3_576
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio4x3_720
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio9x16_1080
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio9x16_480
import com.tejpratapsingh.motionlib.core.VideoAspectRatio.Ratio9x16_720

private val allRatios =
    listOf(
        Ratio1x1_480,
        Ratio1x1_720,
        Ratio1x1_1080,
        Ratio4x3_480,
        Ratio4x3_576,
        Ratio4x3_720,
        Ratio16x9_480,
        Ratio16x9_720,
        Ratio16x9_1080,
        Ratio16x9_1440,
        Ratio16x9_2160,
        Ratio9x16_480,
        Ratio9x16_720,
        Ratio9x16_1080,
        Ratio21x9_1080,
        Ratio21x9_2160,
    )

private val gsonWithAspectRatio =
    GsonBuilder()
        .registerTypeAdapter(
            VideoAspectRatio::class.java,
            JsonDeserializer { element, _, _ ->
                val obj = element.asJsonObject
                val width = if (obj.has("width")) obj.get("width").asInt else 0
                val height = if (obj.has("height")) obj.get("height").asInt else 0
                val label = if (obj.has("label")) obj.get("label").asString else "Custom"

                allRatios.find { it.width == width && it.height == height }
                    ?: Custom(width, height, label)
            },
        ).create()

private val gsonDefault = Gson()

fun VideoAspectRatio.toJson(): JsonObject = gsonDefault.toJsonTree(this).asJsonObject

fun JsonObject.toVideoAspectRatio(): VideoAspectRatio {
    return gsonWithAspectRatio.fromJson(this, VideoAspectRatio::class.java)
}
