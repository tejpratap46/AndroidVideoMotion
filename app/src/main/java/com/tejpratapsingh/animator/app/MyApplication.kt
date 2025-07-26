package com.tejpratapsingh.animator.app

import android.app.Application
import com.tejpratapsingh.animator.notification.NotificationFactory

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationFactory.Companion.createNotificationChannels(this)
    }
}