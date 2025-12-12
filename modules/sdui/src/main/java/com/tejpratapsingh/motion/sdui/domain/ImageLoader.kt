package com.tejpratapsingh.motion.sdui.domain

import android.widget.ImageView

fun interface ImageLoader {
    fun load(
        view: ImageView,
        url: String,
    )
}
