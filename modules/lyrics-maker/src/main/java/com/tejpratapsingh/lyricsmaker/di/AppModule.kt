package com.tejpratapsingh.lyricsmaker.di

import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.ProjectsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.SettingsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.worker.LyricsMotionWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { SettingsViewModel(get()) }
    viewModel { LyricsViewModel(get()) }
    viewModel { ProjectsViewModel(get(), get()) }
    worker { LyricsMotionWorker(get(), get(), get(), get()) }
}
