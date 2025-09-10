package com.tejpratapsingh.motion.sdui.presentation

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.data.SduiRenderer
import com.tejpratapsingh.motion.sdui.domain.ViewFactory

class ContainerFactory : ViewFactory {
    override fun create(context: Context, json: JsonObject, renderer: SduiRenderer): View {
        val layout = LinearLayout(context).apply {
            orientation = if (json.get("orientation")?.asString == "horizontal") LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
        }
        val children = json.getAsJsonArray("children") ?: JsonArray()
        for (i in 0 until children.size()) {
            val childJson = children.get(i).asJsonObject
            val childView = renderer.createView(context, childJson)
            layout.addView(childView)
        }
        return layout
    }
}