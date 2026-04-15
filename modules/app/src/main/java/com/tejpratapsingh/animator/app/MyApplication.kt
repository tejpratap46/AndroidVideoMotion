package com.tejpratapsingh.animator.app

import android.app.Application
import com.tejpratapsingh.animator.notification.NotificationFactory
import com.tejpratapsingh.motionlib.tensorflow.TensorFlowImageProcessor

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationFactory.createNotificationChannels(this)

//        PyTorchImageProcessor.init(applicationContext)
        TensorFlowImageProcessor.init(applicationContext)
    }
}
