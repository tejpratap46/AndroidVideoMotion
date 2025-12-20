package com.tejpratapsingh.lyricsmaker

import android.app.Application
import com.tejpratapsingh.motion.ongoing.domain.ProjectManager

class LyricMaker : Application() {
    override fun onCreate() {
        super.onCreate()
        ProjectManager.initDataBase(applicationContext)
    }
}