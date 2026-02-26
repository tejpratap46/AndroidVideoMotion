package com.tejpratapsingh.motionstore.infra

import com.tejpratapsingh.motionstore.domain.BackendAdapter
import com.tejpratapsingh.motionstore.domain.SyncException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * [BackendAdapter] backed by a custom REST API.
 *
 * Assumes a JSON REST API following these conventions:
 *
 *   GET    /sync/{table}?since=<ms>&deviceId=<id>   → JSON array of rows
 *   POST   /sync/{table}                            → created row (with serverId + uploadedAt)
 *   PUT    /sync/{table}/{serverId}                 → updated row (with uploadedAt)
 *   DELETE /sync/{table}/{serverId}                 → 204 No Content
 *
 * Swap [baseUrl] and [authTokenProvider] to point at any REST backend.
 * For more complex needs (OAuth, certificate pinning, etc.) replace the
 * [HttpURLConnection] calls with OkHttp or Ktor.
 *
 * @param baseUrl           Root URL, e.g. "https://api.example.com"
 * @param authTokenProvider Lambda that returns a Bearer token (or null for
 *                          unauthenticated APIs). Called on every request so
 *                          token refresh is handled transparently.
 */
class RestAdapter(
    private val baseUrl: String,
    private val authTokenProvider: suspend () -> String? = { null },
) : BackendAdapter {
    override suspend fun fetchSince(
        tableName: String,
        since: Long,
        deviceId: String,
    ): List<Map<String, Any?>> {
        val url = "$baseUrl/sync/$tableName?since=$since&deviceId=${deviceId.encode()}"
        val response = request("GET", url)
        return JSONArray(response).toListOfMaps()
    }

    override suspend fun create(
        tableName: String,
        data: Map<String, Any?>,
    ): Map<String, Any?> {
        val url = "$baseUrl/sync/$tableName"
        val response = request("POST", url, body = data.toJsonObject())
        return JSONObject(response).toMap()
    }

    override suspend fun update(
        tableName: String,
        serverId: String,
        data: Map<String, Any?>,
    ): Map<String, Any?> {
        val url = "$baseUrl/sync/$tableName/$serverId"
        val response = request("PUT", url, body = data.toJsonObject())
        return JSONObject(response).toMap()
    }

    override suspend fun delete(
        tableName: String,
        serverId: String,
    ) {
        val url = "$baseUrl/sync/$tableName/$serverId"
        request("DELETE", url)
    }

    // ── HTTP ─────────────────────────────────────────────────────────────────

    private suspend fun request(
        method: String,
        url: String,
        body: JSONObject? = null,
    ): String =
        withContext(Dispatchers.IO) {
            val connection =
                (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = method
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    authTokenProvider()?.let { setRequestProperty("Authorization", "Bearer $it") }
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    if (body != null) {
                        doOutput = true
                        OutputStreamWriter(outputStream).use { it.write(body.toString()) }
                    }
                }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream.bufferedReader().use { it.readText() }

            if (code !in 200..299) {
                throw SyncException.NetworkError(
                    "HTTP $code on $method $url — $responseText",
                )
            }
            responseText
        }

    // ── Serialization helpers ─────────────────────────────────────────────────

    private fun String.encode() = java.net.URLEncoder.encode(this, "UTF-8")

    private fun Map<String, Any?>.toJsonObject(): JSONObject =
        JSONObject().also { json ->
            forEach { (k, v) -> json.put(k, v ?: JSONObject.NULL) }
        }

    private fun JSONArray.toListOfMaps(): List<Map<String, Any?>> = (0 until length()).map { getJSONObject(it).toMap() }

    private fun JSONObject.toMap(): Map<String, Any?> =
        keys().asSequence().associateWith { key ->
            when (val v = get(key)) {
                JSONObject.NULL -> null
                is JSONObject -> v.toMap()
                is JSONArray -> v.toListOfMaps()
                else -> v
            }
        }
}
