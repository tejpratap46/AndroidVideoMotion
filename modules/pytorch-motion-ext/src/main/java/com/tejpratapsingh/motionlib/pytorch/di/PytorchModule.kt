package com.tejpratapsingh.motionlib.pytorch.di

import com.tejpratapsingh.motionlib.pytorch.PyTorchImageProcessor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val pytorchModule = module {
    single { PyTorchImageProcessor(androidContext()) }
}
