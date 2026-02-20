package com.tejpratapsingh.lyricsmaker.data.api.lrclib.client

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.GetParams
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.SearchParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * OkHttp implementation of LRCLIB API service
 */
class LrcLibApiServiceImpl(
    private val client: OkHttpClient,
    private val gson: Gson,
) : LrcLibApiService {
    private val baseUrl: String = "https://lrclib-proxy.tej.workers.dev/api"

    override suspend fun search(params: SearchParams): Result<List<LyricsResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val url =
                    HttpUrl
                        .parse("$baseUrl/search")
                        ?.newBuilder()
                        ?.apply {
                            params.trackName?.let { addQueryParameter("track_name", it) }
                            params.artistName?.let { addQueryParameter("artist_name", it) }
                            params.albumName?.let { addQueryParameter("album_name", it) }
                            params.q?.let { addQueryParameter("q", it) }
                        }?.build()

                if (url == null) {
                    return@withContext Result.failure(IllegalArgumentException("Invalid URL"))
                }

                val request =
                    Request
                        .Builder()
                        .url(url)
                        .header("User-Agent", "LrcLib-Android-Client")
                        .get()
                        .build()

                val response = client.newCall(request).execute()

                response.use {
                    if (!it.isSuccessful) {
                        return@withContext Result.failure(
                            IOException("API request failed with code: ${it.code()}"),
                        )
                    }

                    val body =
                        it.body()?.string()
                            ?: return@withContext Result.failure(IOException("Empty response body"))

                    val type = object : TypeToken<List<LyricsResponse>>() {}.type
                    val lyrics: List<LyricsResponse> = gson.fromJson(body, type)

                    Result.success(lyrics)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun get(params: GetParams): Result<LyricsResponse> =
        withContext(Dispatchers.IO) {
            try {
                val url =
                    HttpUrl
                        .parse("$baseUrl/get")
                        ?.newBuilder()
                        ?.apply {
                            addQueryParameter("track_name", params.trackName)
                            addQueryParameter("artist_name", params.artistName)
                            params.albumName?.let { addQueryParameter("album_name", it) }
                            params.duration?.let { addQueryParameter("duration", it.toString()) }
                        }?.build()

                if (url == null) {
                    return@withContext Result.failure(IllegalArgumentException("Invalid URL"))
                }

                val request =
                    Request
                        .Builder()
                        .url(url)
                        .header("User-Agent", "LrcLib-Android-Client")
                        .get()
                        .build()

                val response = client.newCall(request).execute()

                response.use {
                    if (!it.isSuccessful) {
                        return@withContext Result.failure(
                            IOException("API request failed with code: ${it.code()}"),
                        )
                    }

                    val body =
                        it.body()?.string()
                            ?: return@withContext Result.failure(IOException("Empty response body"))

                    val lyrics: LyricsResponse = gson.fromJson(body, LyricsResponse::class.java)

                    Result.success(lyrics)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
