package com.tejpratapsingh.lyricsmaker

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.tejpratapsingh.lyricsmaker.presentation.view.MultiLyricsContainer
import com.tejpratapsingh.motion.sdui.infra.MotionSdui
import com.tejpratapsingh.motion.sdui.infra.MotionSduiInitializer
import com.tejpratapsingh.motion.sdui.infra.parseMotionViewProps
import com.tejpratapsingh.motionstore.dao.DownloadedTrackerDao
import com.tejpratapsingh.motionstore.dao.MotionProjectDao
import com.tejpratapsingh.motionstore.infra.DatabaseManager
import com.tejpratapsingh.motionstore.infra.FirebaseAdapter
import com.tejpratapsingh.motionstore.infra.SyncManager
import com.tejpratapsingh.motionstore.worker.SyncWorker
import com.tejpratapsingh.motionstore.worker.SyncWorkerFactory
import timber.log.Timber

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

        Timber.plant(Timber.DebugTree())

        FirebaseApp.initializeApp(this)
        SyncWorker.scheduleImmediate(this)
//        SyncWorker.schedulePeriodic(this)

        MotionSduiInitializer.initialize()

        // Register MultiLyricsContainer
        MotionSdui.registerView(MultiLyricsContainer::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps()
            val songName = json.get("songName")?.asString ?: ""
            val image = json.get("image")?.asString
            MultiLyricsContainer(
                context = context!!,
                songName = songName,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                image = image,
            )
        }
        MotionSdui.registerViewSerializer(MultiLyricsContainer::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("songName", view.songName)
            json.addProperty("image", view.image)
        }
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
