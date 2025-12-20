package com.tejpratapsingh.motion.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object ImageLoader {

    fun loadImage(context: Context, url: String) =
        Glide.with(context).load(url)

    suspend fun loadBitmap(context: Context, url: String): Bitmap = suspendCancellableCoroutine { cont ->
        val target = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                cont.resume(resource)
            }
            override fun onLoadCleared(placeholder: Drawable?) {
                if (!cont.isCompleted) cont.cancel()
            }
        }

        Glide.with(context)
            .asBitmap()
            .load(url)
            .into(target)

        cont.invokeOnCancellation {
            Glide.with(context).clear(target)
        }
    }
}