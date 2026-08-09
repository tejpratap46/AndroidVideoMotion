package com.tejpratapsingh.motionlib.mlkit.di

import com.tejpratapsingh.motionlib.mlkit.MLKitImageProcessor
import org.koin.dsl.module

val mlKitModule = module {
    single { MLKitImageProcessor() }
}
