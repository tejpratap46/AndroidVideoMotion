package com.tejpratapsingh.lyricsmaker

import android.app.Application
import android.content.Context
import com.tejpratapsingh.motionstore.dao.MotionProjectDao
import com.tejpratapsingh.motionstore.infra.DatabaseManager

class LyricsApp : Application() {
    val database by lazy { DatabaseManager.init(this) }
    val motionStore by lazy { MotionProjectDao(database) }
}

fun Context.asLyricsApp(): LyricsApp = applicationContext as LyricsApp
