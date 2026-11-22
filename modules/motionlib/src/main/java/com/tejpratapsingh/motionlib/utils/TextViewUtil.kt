package com.tejpratapsingh.motionlib.utils

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File

fun Context.getWebFont(url: String): Typeface? =
    runBlocking(Dispatchers.IO) {
        val client = HttpClient(CIO)
        try {
            val response = client.get(url).body<ByteArray>()
            val fontFile = File(cacheDir, "downloaded_font.ttf")
            fontFile.writeBytes(response)
            Typeface.createFromFile(fontFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            client.close()
        }
    }

fun TextView.getWebFont(url: String): Typeface? = context.getWebFont(url)
