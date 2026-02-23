package com.tejpratapsingh.motion.sdui.infra

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionEffect

fun MotionEffect.toJsonObject(): JsonObject =
    Gson()
        .toJsonTree(
            mapOf(
                "startFrame" to startFrame,
                "endFrame" to startFrame,
            ),
        ).asJsonObject
