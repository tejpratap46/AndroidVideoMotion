package com.tejpratapsingh.lyricsmaker.di

import com.tejpratapsingh.lyricsmaker.data.api.albumart.client.AlbumArtRemoteDataSource
import com.tejpratapsingh.lyricsmaker.data.api.albumart.client.AlbumArtRemoteDataSourceImpl
import com.tejpratapsingh.lyricsmaker.data.api.albumart.client.AlbumArtRepository
import com.tejpratapsingh.lyricsmaker.data.api.albumart.client.AlbumArtRepositoryImpl
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.client.LrcLibApiService
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.client.LrcLibApiServiceImpl
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.client.LyricsRepository
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.client.LyricsRepositoryImpl
import com.tejpratapsingh.lyricsmaker.data.store.RecentSearchHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val lyricsDataModule = module {
    single<LrcLibApiService> { LrcLibApiServiceImpl(get(), get()) }
    single<LyricsRepository> { LyricsRepositoryImpl(get()) }
    single<AlbumArtRemoteDataSource> { AlbumArtRemoteDataSourceImpl(get()) }
    single<AlbumArtRepository> { AlbumArtRepositoryImpl(get()) }
    single { RecentSearchHelper(androidContext()) }
}
