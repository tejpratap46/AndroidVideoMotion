package com.tejpratapsingh.motionstore.infra

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * A no-op [ContentProvider] whose sole purpose is to initialize [DeviceInfo]
 * automatically at app startup — before any Activity, Service, or BroadcastReceiver.
 *
 * Android instantiates all declared ContentProviders and calls their [onCreate]
 * during the app launch sequence, providing a [Context] with no manual wiring needed.
 *
 * Declare in AndroidManifest.xml:
 *
 *   <provider
 *       android:name=".DeviceInfoInitializer"
 *       android:authorities="${applicationId}.device-info-init"
 *       android:exported="false" />
 */
class DeviceInfoInitializer : ContentProvider() {
    override fun onCreate(): Boolean {
        DeviceInfo.initialize(context!!)
        return true
    }

    // ── Unused ContentProvider methods ────────────────────────────────────────

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int = 0
}
