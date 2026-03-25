package com.tejpratapsingh.motionstore.infra

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.UUID

/**
 * Provides a stable, unique device identifier that persists across app launches.
 *
 * Uses explicit initialization via a [ContentProvider] that runs automatically
 * at app startup — no manual init() call required anywhere in your code.
 *
 * The [ContentProvider] trick is the same mechanism used by WorkManager, Firebase,
 * and other zero-init libraries. Android guarantees it runs before any Activity,
 * Service, or BroadcastReceiver starts.
 *
 * ── Setup ────────────────────────────────────────────────────────────────────
 *
 * Add DeviceInfoInitializer to your AndroidManifest.xml:
 *
 *   <provider
 *       android:name=".DeviceInfoInitializer"
 *       android:authorities="${applicationId}.device-info-init"
 *       android:exported="false" />
 *
 * That's it. No Application.onCreate() call needed.
 *
 * ── Usage ────────────────────────────────────────────────────────────────────
 *
 *   val deviceId = DeviceInfo.id   // works from Activity, Service, anywhere
 */
object DeviceInfo {
    private const val PREFS_FILE = "device_info_prefs"
    private const val KEY_DEVICE_ID = "device_id"

    private lateinit var prefs: SharedPreferences

    /**
     * Stable device UUID. Generated once and persisted to SharedPreferences.
     * Accessing this before the ContentProvider has run will throw — but that
     * cannot happen in normal app flow since the provider runs before everything.
     */
    val id: String by lazy {
        check(::prefs.isInitialized) {
            "DeviceInfo not initialized. Make sure DeviceInfoInitializer is declared in AndroidManifest.xml."
        }
        prefs.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().also { newId ->
            prefs.edit(true) { putString(KEY_DEVICE_ID, newId) }
        }
    }

    /** Called exclusively by [DeviceInfoInitializer]. Not part of the public API. */
    internal fun initialize(context: Context) {
        prefs =
            context.applicationContext
                .getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
    }
}
