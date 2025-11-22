package com.tejpratapsingh.lyricsmaker.domain

fun <T> List<T>.ensureArrayList(): ArrayList<T> {
    return this as? ArrayList<T> ?: ArrayList(this)
}