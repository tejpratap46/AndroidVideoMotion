package com.tejpratapsingh.motion.sdui.presentation

import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.data.SduiRenderer
import com.tejpratapsingh.motion.sdui.domain.ViewFactory

class TextFactory : ViewFactory {
    override fun create(context: Context, json: JsonObject, renderer: SduiRenderer): View {
        return TextView(context).apply {
            text = json.get("text")?.asString ?: ""
            textSize = json.get("textSize")?.asFloat ?: 16f
        }
    }
}
