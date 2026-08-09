package com.tejpratapsingh.motion.sdui.di

import com.tejpratapsingh.motion.sdui.data.SduiRenderer
import org.koin.dsl.module

val sduiModule = module {
    single { SduiRenderer(getOrNull()) }
}
