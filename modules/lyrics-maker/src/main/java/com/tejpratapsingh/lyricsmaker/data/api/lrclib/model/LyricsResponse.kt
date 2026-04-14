package com.tejpratapsingh.lyricsmaker.data.api.lrclib.model

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
data class LyricsResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("trackName")
    val trackName: String,
    @SerializedName("artistName")
    val artistName: String,
    @SerializedName("albumName")
    val albumName: String?,
    @SerializedName("duration")
    val duration: Double,
    @SerializedName("instrumental")
    val instrumental: Boolean,
    @SerializedName("plainLyrics")
    val plainLyrics: String?,
    @SerializedName("syncedLyrics")
    val syncedLyrics: String?,
) : Parcelable {
    fun getLyrics(): String =
        if (syncedLyrics.isNullOrEmpty()) {
            "[0:00.00] No Lyrics Found"
        } else {
            syncedLyrics
        }

    fun getReadableDuration(): String {
        val totalSeconds = duration.toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d min and %02d sec", minutes, seconds)
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
