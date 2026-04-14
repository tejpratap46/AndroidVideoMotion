package com.tejpratapsingh.motionlib.utils

import android.graphics.Typeface
import android.widget.TextView
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.File

fun TextView.getWebFont(url: String): Typeface? =
    runBlocking(Dispatchers.IO) {
        val client = HttpClient(CIO)
        try {
            val response = client.get(url).body<ByteArray>()
            val fontFile = File(context.cacheDir, "downloaded_font.ttf")
            fontFile.writeBytes(response)
            Typeface.createFromFile(fontFile)
        } catch (e: Exception) {
            Timber.e(e, "getWebFont failed for url: $url")
            null
        } finally {
            client.close()
        }
    }
