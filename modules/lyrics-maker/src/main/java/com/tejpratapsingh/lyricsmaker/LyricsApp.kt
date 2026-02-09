package com.tejpratapsingh.lyricsmaker

import android.app.Application
import android.content.Context
import com.tejpratapsingh.motionstore.infra.provideMotionDbHelper

class LyricsApp : Application() {
    val database by lazy { provideMotionDbHelper(this) }
}

fun Context.asLyricsApp(): LyricsApp = applicationContext as LyricsApp
