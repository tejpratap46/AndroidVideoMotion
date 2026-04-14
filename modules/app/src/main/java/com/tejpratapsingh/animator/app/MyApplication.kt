package com.tejpratapsingh.animator.app

import android.app.Application
import com.tejpratapsingh.animator.BuildConfig
import com.tejpratapsingh.animator.notification.NotificationFactory
import com.tejpratapsingh.motionlib.tensorflow.TensorFlowImageProcessor
import timber.log.Timber

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.i("MyApplication created")

        NotificationFactory.createNotificationChannels(this)

//        PyTorchImageProcessor.init(applicationContext)
        TensorFlowImageProcessor.init(applicationContext)
    }
}
