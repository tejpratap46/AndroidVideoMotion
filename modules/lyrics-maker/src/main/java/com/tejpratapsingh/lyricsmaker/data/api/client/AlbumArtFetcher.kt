package com.tejpratapsingh.lyricsmaker.data.api.client

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object AlbumArtFetcher {
    private val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    },
                )
            }
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "musicbrainz.org"
                }
                headers.append(
                    HttpHeaders.UserAgent,
                    "AlbumArtFetcher/1.0 (lyrics@tejpratapsingh.com)",
                )
            }
        }

    enum class CoverSize(
        val suffix: String,
    ) {
        ORIGINAL(""), // full resolution
        SMALL("-250"),
        MEDIUM("-500"),
        LARGE("-1200"),
    }

    suspend fun fetchAlbumArtUrl(
        trackName: String,
        artistName: String,
        size: CoverSize = CoverSize.SMALL,
    ): String? {
        val response: MusicBrainzResponse =
            client
                .get("/ws/2/recording") {
                    parameter("query", "recording:\"$trackName\" AND artist:\"$artistName\"")
                    parameter("fmt", "json")
                }.body()

        val releaseId =
            response.recordings
                .firstOrNull()
                ?.releases
                ?.firstOrNull()
                ?.id ?: return null

        // Add size suffix
        return "https://coverartarchive.org/release/$releaseId/front${size.suffix}"
    }

    suspend fun fetchAlbumArtBitmap(url: String): Bitmap? =
        try {
            val bytes: ByteArray = client.get(url).body()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    fun close() {
        client.close()
    }
}

@Serializable
data class MusicBrainzResponse(
    val recordings: List<Recording> = emptyList(),
)

@Serializable
data class Recording(
    val releases: List<Release> = emptyList(),
)

@Serializable
data class Release(
    val id: String,
)
