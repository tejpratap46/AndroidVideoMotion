package com.tejpratapsingh.lyricsmaker.data.api.model

import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class LyricsResponse(
    val id: Int,
    val trackName: String,
    val artistName: String,
    val albumName: String? = null,
    val duration: Float? = null,
    val instrumental: Boolean = false,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null,
) : Parcelable {
    fun getLyrics(): String {
        return if (syncedLyrics.isNullOrEmpty()) {
            "[0:00.00] No Lyrics Found"
        } else {
            syncedLyrics
        }
    }

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    companion object {
        fun fromJson(json: String): LyricsResponse {
            val gson = Gson()
            return gson.fromJson(json, LyricsResponse::class.java)
        }
    }
}
