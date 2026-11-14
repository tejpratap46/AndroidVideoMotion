package com.tejpratapsingh.motion.sdui.di

import com.tejpratapsingh.motion.sdui.data.SduiRenderer
import com.tejpratapsingh.motion.sdui.infra.SDUIMotionVideoProducerFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val sduiModule = module {
    single { SduiRenderer(getOrNull()) }
    single {
        SDUIMotionVideoProducerFactory(
            context = androidContext(),
            videoProducerAdapter = get(),
        )
    }
}
