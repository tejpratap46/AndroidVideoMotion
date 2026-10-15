package com.tejpratapsingh.motion.assetstore.di

import com.tejpratapsingh.motion.assetstore.repository.AssetStoreRepository
import com.tejpratapsingh.motion.assetstore.service.ExternalAssetService
import com.tejpratapsingh.motion.assetstore.service.pexels.PexelsAssetService
import com.tejpratapsingh.motion.assetstore.service.pexels.PexelsConfig
import com.tejpratapsingh.motion.assetstore.ui.AssetPickerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val assetStoreModule =
    module {
        single { PexelsConfig() }
        single<ExternalAssetService> {
            PexelsAssetService(
                client = get(),
                gson = get(),
                config = get(),
            )
        }
        single {
            AssetStoreRepository(
                context = androidContext(),
                services = listOf(get<ExternalAssetService>()),
                ketch = get(),
                kv = get(named("motion_download_cache")),
                assetManager = get(),
            )
        }
        viewModel { AssetPickerViewModel(get()) }
    }
