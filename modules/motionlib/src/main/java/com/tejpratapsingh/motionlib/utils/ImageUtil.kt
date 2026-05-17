package com.tejpratapsingh.motionlib.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream

object ImageUtil {
    private val client = OkHttpClient()

    suspend fun fetchBitmap(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext when (uri.scheme) {
            "http", "https" -> fetchFromNetwork(uri.toString())
            "content", "file", "android.resource" -> fetchFromLocal(context, uri)
            else -> null
        }
    }

    private fun fetchFromNetwork(url: String): Bitmap? {
        val request = Request.Builder().url(url).build()
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val bytes = response.body()?.bytes() ?: return null
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchFromLocal(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
    }
}
