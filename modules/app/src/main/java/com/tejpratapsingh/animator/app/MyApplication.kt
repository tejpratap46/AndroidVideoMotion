package com.tejpratapsingh.animator.app

import android.app.Application
import com.tejpratapsingh.animator.notification.NotificationFactory
import com.tejpratapsingh.motionlib.core.di.coreModule
import com.tejpratapsingh.motionlib.tensorflow.di.tensorflowModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        NotificationFactory.createNotificationChannels(this)

        startKoin {
            androidContext(this@MyApplication)
            modules(coreModule, tensorflowModule)
        }
    }
}
