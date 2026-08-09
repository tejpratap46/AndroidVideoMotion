package com.tejpratapsingh.lyricsmaker.di

import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { SettingsViewModel(get()) }
    viewModel { LyricsViewModel(get()) }
}
