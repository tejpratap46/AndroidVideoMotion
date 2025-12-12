package com.tejpratapsingh.lyricsmaker.data.lrc

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class SyncedLyricFrame(
    val frame: Int,
    val text: String,
) : Parcelable
