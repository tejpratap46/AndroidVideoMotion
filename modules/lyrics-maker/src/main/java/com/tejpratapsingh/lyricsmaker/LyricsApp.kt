package com.tejpratapsingh.lyricsmaker

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.tejpratapsingh.motionstore.dao.DownloadedTrackerDao
import com.tejpratapsingh.motionstore.dao.MotionProjectDao
import com.tejpratapsingh.motionstore.infra.DatabaseManager
import com.tejpratapsingh.motionstore.infra.FirebaseAdapter
import com.tejpratapsingh.motionstore.infra.SyncManager
import com.tejpratapsingh.motionstore.worker.SyncWorker
import com.tejpratapsingh.motionstore.worker.SyncWorkerFactory

class LyricsApp :
    Application(),
    Configuration.Provider {
    val database by lazy { DatabaseManager.init(this) }
    val motionStoreDao by lazy { MotionProjectDao(database) }

    val syncManager by lazy {
        SyncManager(
            backend = FirebaseAdapter(),
            downloadedTracker = DownloadedTrackerDao(database),
        )
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        SyncWorker.scheduleImmediate(this)
//        SyncWorker.schedulePeriodic(this)
    }

    /**
     * Provides a custom WorkManager configuration injecting [SyncManager]
     * and DAOs into [SyncWorker] via [SyncWorkerFactory].
     *
     * Implementing [Configuration.Provider] here replaces the default
     * WorkManager auto-init. Remove the WorkManagerInitializer <meta-data>
     * entry from AndroidManifest.xml (see SyncWorker KDoc for the snippet).
     */

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(
                    SyncWorkerFactory(
                        syncManager = syncManager,
                        daos = listOf(motionStoreDao),
                    ),
                ).build()
}

fun Context.asLyricsApp(): LyricsApp = applicationContext as LyricsApp
