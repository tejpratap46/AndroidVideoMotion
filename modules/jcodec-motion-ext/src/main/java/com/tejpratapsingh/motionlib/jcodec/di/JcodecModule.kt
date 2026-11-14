package com.tejpratapsingh.motionlib.jcodec.di

import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.jcodec.JCodecVideoProducerAdapter
import org.koin.dsl.module

val jcodecModule = module {
    single<VideoProducerAdapter> { JCodecVideoProducerAdapter() }
}
