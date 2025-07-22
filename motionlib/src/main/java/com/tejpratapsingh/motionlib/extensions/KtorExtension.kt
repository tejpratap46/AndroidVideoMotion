package com.tejpratapsingh.motionlib.extensions

import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel // More explicit way to get the channel
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.util.cio.writeChannel // For File.writeChannel()
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException // For more specific IO exceptions

// Custom exception for better error handling if desired
class DownloadException(message: String, cause: Throwable? = null) : IOException(message, cause)

suspend fun HttpClient.downloadFile(
    file: File,
    url: String
): File {
    try {
        val response: HttpResponse = this.request { // Changed 'call' to 'response' for clarity
            url(url)
            method = HttpMethod.Get
        }

        if (!response.status.isSuccess()) {
            throw DownloadException("Error downloading file from $url: HTTP ${response.status}")
        }

        // Get the ByteReadChannel from the response
        val byteReadChannel = response.bodyAsChannel()

        // Switch to Dispatchers.IO for file writing operations
        return withContext(Dispatchers.IO) {
            try {
                // Ensure parent directory exists
                file.parentFile?.mkdirs() // Create parent directories if they don't exist

                val fileWriteChannel = file.writeChannel()
                byteReadChannel.copyAndClose(fileWriteChannel)
                file
            } catch (e: IOException) {
                // Catch specific IOExceptions during file writing
                throw DownloadException("Failed to write downloaded file to ${file.path}: ${e.message}", e)
            }
        }
    } catch (e: DownloadException) {
        // Re-throw our custom exception
        throw e
    } catch (e: Exception) {
        // Catch other potential exceptions (e.g., Ktor client exceptions)
        throw DownloadException("An unexpected error occurred during download from $url: ${e.message}", e)
    }
}
