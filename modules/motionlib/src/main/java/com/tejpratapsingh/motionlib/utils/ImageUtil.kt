package com.tejpratapsingh.motionlib.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.tejpratapsingh.motionlib.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.InputStream

object ImageUtil {
    private val client = OkHttpClient()

    suspend fun fetchBitmap(
        context: Context,
        uri: Uri,
    ): Bitmap? =
        withContext(Dispatchers.IO) {
            val bitmap =
                when (uri.scheme) {
                    "http", "https" -> fetchFromNetwork(uri.toString())
                    "content", "file", "android.resource" -> fetchFromLocal(context, uri)
                    else -> null
                }
            return@withContext bitmap ?: fetchDefault(context)
        }

    private fun fetchDefault(context: Context): Bitmap? =
        try {
            BitmapFactory.decodeResource(context.resources, R.drawable.default_bg)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }

    private fun fetchFromNetwork(url: String): Bitmap? =
        try {
            if (url.isBlank()) {
                null
            } else {
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return null
                    val bytes = response.body()?.bytes() ?: return null
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            }
        } catch (e: Exception) {
            null
        }

    private fun fetchFromLocal(
        context: Context,
        uri: Uri,
    ): Bitmap? =
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
}
