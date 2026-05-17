package com.tejpratapsingh.lyricsmaker.data.api.albumart.client

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.tejpratapsingh.lyricsmaker.data.api.albumart.data.MusicBrainzResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber

class AlbumArtRemoteDataSourceImpl(
    private val client: OkHttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : AlbumArtRemoteDataSource {
    override suspend fun fetchAlbumArtUrl(
        trackName: String,
        artistName: String,
        size: AlbumArtRepository.CoverSize,
    ): String? =
        withContext(Dispatchers.IO) {
            try {
                val url =
                    HttpUrl
                        .Builder()
                        .scheme("https")
                        .host("musicbrainz.org")
                        .addPathSegments("ws/2/recording")
                        .addQueryParameter(
                            "query",
                            "recording:\"$trackName\" AND artist:\"$artistName\"",
                        ).addQueryParameter("fmt", "json")
                        .build()

                val request =
                    Request
                        .Builder()
                        .url(url)
                        .header(
                            "User-Agent",
                            "AlbumArtFetcher/1.0 (lyrics@tejpratapsingh.com)",
                        ).build()

                client.newCall(request).execute().use { response ->

                    if (!response.isSuccessful) return@withContext null

                    val bodyString = response.body()?.string() ?: return@withContext null

                    val parsed = json.decodeFromString<MusicBrainzResponse>(bodyString)

                    val releaseId =
                        parsed.recordings
                            .firstOrNull()
                            ?.releases
                            ?.firstOrNull()
                            ?.id ?: return@withContext null

                    "https://coverartarchive.org/release/$releaseId/front${size.suffix}"
                }
            } catch (e: Exception) {
                Timber.e(e, "fetchAlbumArtUrl failed")
                null
            }
        }

    override suspend fun fetchBitmap(url: String): Bitmap? =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val bytes = response.body()?.bytes() ?: return@withContext null
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }
}
