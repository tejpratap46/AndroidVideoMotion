package com.tejpratapsingh.motionlib.media3.di

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.media3.Media3VideoProducerAdapter
import org.koin.dsl.module

@OptIn(UnstableApi::class)
val media3Module = module {
    single<VideoProducerAdapter> { Media3VideoProducerAdapter() }
}
