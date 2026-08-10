package com.tejpratapsingh.motion.download.di

import com.ketch.Ketch
import com.tejpratapsingh.motion.download.MotionAssetManagerImpl
import com.tejpratapsingh.motion.download.MotionDownloadController
import com.tejpratapsingh.motion.download.MotionDownloadWorker
import com.tejpratapsingh.motion.download.ui.MotionDownloadViewModel
import com.tejpratapsingh.motionlib.core.MotionAssetManager
import com.tencent.mmkv.MMKV
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val downloadModule =
    module {
        single {
            MotionAssetManagerImpl(
                context = androidContext(),
                ketch = get(),
                kv = get(named("motion_download_cache")),
            )
        }
        single<MotionAssetManager> { get<MotionAssetManagerImpl>() }

        single { Ketch.builder().build(androidContext()) }
        single(named("motion_download_cache")) {
            MMKV.initialize(androidContext())
            MMKV.mmkvWithID("motion_download_cache")
        }

        single {
            MotionDownloadController(
                context = androidContext(),
                projectDao = get(),
                ketch = get(),
                kv = get(named("motion_download_cache")),
                assetManager = get(),
            )
        }

        worker {
            MotionDownloadWorker(
                context = get(),
                params = get(),
                controller = get(),
            )
        }

        viewModel { MotionDownloadViewModel(get()) }
    }
