package com.tejpratapsingh.lyricsmaker.domain

import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
enum class TrimUnit : Parcelable {
    FRAME, MILLI_SECOND
}

@Serializable
@Parcelize
data class TrimLyrics(val start: Int, val end: Int, val unit: TrimUnit) : Parcelable {
    companion object {
        val NO_TRIM = TrimLyrics(0, Int.MAX_VALUE, TrimUnit.FRAME)

        fun fromJson(json: String): TrimLyrics {
            val gson = Gson()
            return gson.fromJson(json, TrimLyrics::class.java)
        }
    }

    fun getEndFrame(fps: Int): Int {
        return when (unit) {
            TrimUnit.FRAME -> end
            TrimUnit.MILLI_SECOND -> (end / (1000.0 / fps)).toInt()
        }
    }

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}
