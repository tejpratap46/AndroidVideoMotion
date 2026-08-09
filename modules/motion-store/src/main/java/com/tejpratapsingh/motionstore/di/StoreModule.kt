package com.tejpratapsingh.motionstore.di

import com.tejpratapsingh.motionstore.dao.DownloadedTrackerDao
import com.tejpratapsingh.motionstore.infra.DatabaseManager
import com.tejpratapsingh.motionstore.infra.PreferenceManager
import com.tejpratapsingh.motionstore.infra.SyncManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val storeModule = module {
    single { DatabaseManager.getInstance() }
    single { DownloadedTrackerDao(get()) }
    single { PreferenceManager(androidContext()) }
    single { SyncManager(get(), get()) }
}
