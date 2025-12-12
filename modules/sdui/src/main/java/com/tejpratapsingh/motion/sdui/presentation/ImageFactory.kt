package com.tejpratapsingh.motion.sdui.presentation

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.data.SduiRenderer
import com.tejpratapsingh.motion.sdui.domain.ViewFactory

class ImageFactory : ViewFactory {
    override fun create(
        context: Context,
        json: JsonObject,
        renderer: SduiRenderer,
    ): View {
        val imageView = ImageView(context)
        renderer.imageLoader?.load(imageView, json.get("url")?.asString ?: "")
        return imageView
    }
}
