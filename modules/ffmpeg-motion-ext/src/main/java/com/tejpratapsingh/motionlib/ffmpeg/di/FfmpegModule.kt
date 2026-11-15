package com.tejpratapsingh.motionlib.ffmpeg.di

import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.ffmpeg.FfmpegVideoProducerAdapter
import org.koin.dsl.module

val ffmpegModule = module {
    single<VideoProducerAdapter> { FfmpegVideoProducerAdapter() }
}
