package com.tejpratapsingh.motionlib.tensorflow.di

import com.tejpratapsingh.motionlib.tensorflow.TensorFlowImageProcessor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val tensorflowModule = module {
    single { TensorFlowImageProcessor(androidContext()) }
}
