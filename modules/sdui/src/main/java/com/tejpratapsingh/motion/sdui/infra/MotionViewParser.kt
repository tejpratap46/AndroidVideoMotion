package com.tejpratapsingh.motion.sdui.infra

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionView

fun MotionView.toJsonObject(): JsonObject =
    Gson()
        .toJsonTree(
            mapOf(
                "startFrame" to startFrame,
                "endFrame" to startFrame,
                "loop" to
                    JsonObject().apply {
                        addProperty("start", loop.first)
                        addProperty("end", loop.second)
                    },
            ),
        ).asJsonObject
