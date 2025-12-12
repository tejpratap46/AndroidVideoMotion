package com.tejpratapsingh.motion.sdui.domain

import android.content.Context
import com.google.gson.JsonObject

fun interface ActionHandler {
    fun handle(
        context: Context,
        action: JsonObject,
    )
}
