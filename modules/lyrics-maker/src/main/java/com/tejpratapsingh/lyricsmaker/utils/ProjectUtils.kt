package com.tejpratapsingh.lyricsmaker.utils

import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame

fun getSyncedLyricFrameList(data: List<String>) =
    if (data.isEmpty()){
        emptyList()
    }else{
        data.map {
            val (frame, text) = it.split(":")
            SyncedLyricFrame(
                frame = frame.toInt(),
                text = text
            )
        }
    }

fun getSyncedLyricFrameStringList(data: List<SyncedLyricFrame>) =
    data.joinToString(separator = ",") { it.line() }