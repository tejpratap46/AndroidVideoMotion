package com.tejpratapsingh.lyricsmaker.data.api.client
import com.tejpratapsingh.lyricsmaker.data.api.model.LyricsQuery
import com.tejpratapsingh.lyricsmaker.data.api.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.data.api.model.SearchQuery
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class LrcLibClient(
    private val baseUrl: String = "https://lrclib.net/api",
    private val apiKey: String? = null
) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun searchLyrics(query: SearchQuery): List<LyricsResponse> {
        return client.get("$baseUrl/search") {
            parameter("q", query.searchTerm)
        }.body()
    }

    suspend fun getLyrics(query: LyricsQuery): LyricsResponse? {
        val response = client.get("$baseUrl/get") {
            query.id?.let { parameter("id", it) }
            query.trackName?.let { parameter("track_name", it) }
            query.artistName?.let { parameter("artist_name", it) }
            query.albumName?.let { parameter("album_name", it) }
            query.duration?.let { parameter("duration", it) }
        }
        return if (response.status.value == 404) null else response.body()
    }

    // Utility: parse synced lyrics LRC format to lines with timestamps
    fun parseSyncedLyrics(lrc: String): List<Pair<Long, String>> {
        val regex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2})\\](.*)")
        return lrc.lines().mapNotNull {
            val match = regex.matchEntire(it.trim())
            match?.let {
                val (m, s, cs, text) = it.destructured
                val ms = m.toLong() * 60_000 + s.toLong() * 1000 + cs.toLong() * 10
                Pair(ms, text.trim())
            }
        }
    }
}
