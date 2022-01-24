package com.tejpratapsingh.motionlib.extensions

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import java.io.File

suspend fun HttpClient.downloadFile(
    file: File,
    url: String
): File {
    val call: HttpResponse = this.request {
        url(url)
        method = HttpMethod.Get
    }
    if (!call.status.isSuccess()) {
        throw Exception("Error downloading file")
    }
    call.content.copyAndClose(file.writeChannel())
    return file
}