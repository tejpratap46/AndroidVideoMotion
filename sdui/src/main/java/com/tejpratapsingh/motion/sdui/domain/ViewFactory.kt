package com.tejpratapsingh.motion.sdui.domain

import android.content.Context
import android.view.View
import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.data.SduiRenderer

fun interface ViewFactory {
    fun create(context: Context, json: JsonObject, renderer: SduiRenderer): View
}