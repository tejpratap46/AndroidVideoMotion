package com.tejpratapsingh.lyricsmaker.domain

fun <T> List<T>.ensureArrayList(): ArrayList<T> = this as? ArrayList<T> ?: ArrayList(this)
