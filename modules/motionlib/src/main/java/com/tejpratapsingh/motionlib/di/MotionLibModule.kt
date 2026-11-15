package com.tejpratapsingh.motionlib.di

import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.core.adapter.AndroidVideoProducerAdapter
import org.koin.dsl.module

val motionLibModule = module {
    single<VideoProducerAdapter> { AndroidVideoProducerAdapter() }
}
