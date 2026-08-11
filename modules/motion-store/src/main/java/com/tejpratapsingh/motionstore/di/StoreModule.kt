package com.tejpratapsingh.motionstore.di

import com.tejpratapsingh.motionstore.dao.DownloadedTrackerDao
import com.tejpratapsingh.motionstore.dao.MotionProjectDao
import com.tejpratapsingh.motionstore.dao.SyncableDao
import com.tejpratapsingh.motionstore.domain.BackendAdapter
import com.tejpratapsingh.motionstore.infra.DatabaseManager
import com.tejpratapsingh.motionstore.infra.FirebaseAdapter
import com.tejpratapsingh.motionstore.infra.PreferenceManager
import com.tejpratapsingh.motionstore.infra.SyncManager
import com.tejpratapsingh.motionstore.worker.SyncWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val storeModule =
    module {
        single { DatabaseManager.getInstance() }
        single<BackendAdapter> { FirebaseAdapter() }
        single { MotionProjectDao(get()) }
        single { DownloadedTrackerDao(get()) }
        single { PreferenceManager(androidContext()) }
        single { SyncManager(get(), get()) }

        single<List<SyncableDao<*>>> { listOf(get<MotionProjectDao>()) }

        worker {
            SyncWorker(
                context = get(),
                params = get(),
                syncManager = get(),
                daos = get(),
            )
        }
    }
