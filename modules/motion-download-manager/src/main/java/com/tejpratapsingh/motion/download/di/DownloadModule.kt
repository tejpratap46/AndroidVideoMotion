package com.tejpratapsingh.motion.download.di

import com.tejpratapsingh.motion.download.MotionAssetManagerImpl
import com.tejpratapsingh.motion.download.ui.MotionDownloadViewModel
import com.tejpratapsingh.motionlib.core.MotionAssetManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// Assuming SettingsViewModel is in lyricsmaker.presentation.viewmodel
// But this module is in motion-download-manager.
// I'll define it here if possible or just use a generic way.
// Actually, it's better to keep it in the app module.

val downloadModule =
    module {
        single { MotionAssetManagerImpl(androidContext()) }
        single<MotionAssetManager> { get<MotionAssetManagerImpl>() }
        viewModel { MotionDownloadViewModel(get()) }
    }
