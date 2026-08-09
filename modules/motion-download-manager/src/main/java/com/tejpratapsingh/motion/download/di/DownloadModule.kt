package com.tejpratapsingh.motion.download.di

import com.tejpratapsingh.motion.download.MotionDownloadManager
import com.tejpratapsingh.motion.download.ui.MotionDownloadViewModel
import com.tejpratapsingh.motionlib.core.MotionCacheManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

// Assuming SettingsViewModel is in lyricsmaker.presentation.viewmodel
// But this module is in motion-download-manager.
// I'll define it here if possible or just use a generic way.
// Actually, it's better to keep it in the app module.

val downloadModule = module {
    single { MotionDownloadManager(androidContext()) }
    single<MotionCacheManager> { get<MotionDownloadManager>() }
    viewModel { MotionDownloadViewModel(get()) }
}
