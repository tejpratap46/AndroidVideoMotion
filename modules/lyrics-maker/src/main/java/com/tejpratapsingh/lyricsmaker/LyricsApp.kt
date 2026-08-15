package com.tejpratapsingh.lyricsmaker

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.tejpratapsingh.lyricsmaker.di.appModule
import com.tejpratapsingh.lyricsmaker.di.lyricsDataModule
import com.tejpratapsingh.lyricsmaker.domain.sdui.MultiLyricsContainerSdui
import com.tejpratapsingh.motion.download.di.downloadModule
import com.tejpratapsingh.motion.sdui.infra.MotionSduiInitializer
import com.tejpratapsingh.motion.tts.TTSAudioAssetSdui
import com.tejpratapsingh.motionlib.core.di.coreModule
import com.tejpratapsingh.motionstore.dao.MotionProjectDao
import com.tejpratapsingh.motionstore.di.storeModule
import com.tejpratapsingh.motionstore.infra.DatabaseManager
import com.tejpratapsingh.motionstore.infra.PreferenceManager
import com.tejpratapsingh.motionstore.worker.SyncWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import timber.log.Timber

class LyricsApp :
    Application(),
    Configuration.Provider,
    KoinComponent {
    val motionStoreDao: MotionProjectDao by inject()
    val preferenceManager: PreferenceManager by inject()

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        DatabaseManager.init(this)

        startKoin {
            androidContext(this@LyricsApp)
            modules(coreModule, downloadModule, lyricsDataModule, appModule, storeModule)
        }

        FirebaseApp.initializeApp(this)
//        SyncWorker.scheduleImmediate(this)
        SyncWorker.schedulePeriodic(this)

        MotionSduiInitializer.initialize()
        MultiLyricsContainerSdui.register()
        TTSAudioAssetSdui.register()
    }

    /**
     * Provides a custom WorkManager configuration injecting workers via [KoinWorkerFactory].
     *
     * Implementing [Configuration.Provider] here replaces the default
     * WorkManager auto-init. Remove the WorkManagerInitializer <meta-data>
     * entry from AndroidManifest.xml.
     */
    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(KoinWorkerFactory())
                .build()
}

fun Context.asLyricsApp(): LyricsApp = applicationContext as LyricsApp
