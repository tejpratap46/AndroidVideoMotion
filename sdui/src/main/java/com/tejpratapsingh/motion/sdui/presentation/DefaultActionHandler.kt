package com.tejpratapsingh.motion.sdui.presentation

import android.content.Context
import android.widget.Toast
import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.domain.ActionHandler

class DefaultActionHandler : ActionHandler {
    override fun handle(context: Context, action: JsonObject) {
        val type = action.get("type")?.asString
        when (type) {
            "toast" -> Toast.makeText(context, action.get("message")?.asString ?: "", Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(context, "Unknown action: $type", Toast.LENGTH_SHORT).show()
        }
    }
}