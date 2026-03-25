package com.tejpratapsingh.motionstore.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.tejpratapsingh.motionstore.infra.DatabaseManager
import com.tejpratapsingh.motionstore.tables.DownloadedTracker

/**
 * Tracks the high-water mark of downloaded server data per table.
 *
 * One row per table. The [downloadedTill] value is the highest [SyncTracker.uploadedAt]
 * seen from the server so far. On the next sync cycle, we ask the server for all rows
 * where uploadedAt > downloadedTill, giving us a clean incremental download cursor.
 * extending [BaseDao] — it is framework-internal and has no sync metadata of its own.
 */
class DownloadedTrackerDao(
    val dbManager: DatabaseManager,
) : BaseDao<DownloadedTracker>(dbManager) {
    companion object {
        const val TABLE_NAME = "downloaded_tracker"

        const val COL_TABLE_NAME = "table_name"
        const val COL_DOWNLOADED_TILL = "downloaded_till"

        val SCHEMA =
            """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COL_TABLE_NAME      TEXT    PRIMARY KEY,
                $COL_DOWNLOADED_TILL INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
    }

    override val tableName: String
        get() = TABLE_NAME

    override val primaryKey: String
        get() = COL_TABLE_NAME

    override fun fromCursor(cursor: Cursor): DownloadedTracker =
        DownloadedTracker(
            tableName = cursor.getString(cursor.getColumnIndexOrThrow(COL_TABLE_NAME)),
            downloadedTill = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DOWNLOADED_TILL)),
        )

    override fun toContentValues(entity: DownloadedTracker): ContentValues =
        ContentValues().apply {
            put(COL_TABLE_NAME, entity.tableName)
            put(COL_DOWNLOADED_TILL, entity.downloadedTill)
        }

    /**
     * Returns the epoch-ms timestamp up to which [tableName] data has been downloaded.
     * Returns 0 if no download has occurred yet (meaning: fetch everything from the server).
     */
    fun getDownloadedTill(tableName: String): Long =
        dbManager
            .getDb()
            .query(
                TABLE_NAME,
                arrayOf(COL_DOWNLOADED_TILL),
                "$COL_TABLE_NAME = ?",
                arrayOf(tableName),
                null,
                null,
                null,
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_DOWNLOADED_TILL))
                } else {
                    0L
                }
            }

    /**
     * Upsert the cursor for [tableName] to [timestamp].
     * Only advances the cursor — never moves it backwards, since
     * a lower timestamp would cause re-downloading already-synced rows.
     */
    fun setDownloadedTill(
        tableName: String,
        timestamp: Long,
    ) {
        val current = getDownloadedTill(tableName)
        if (timestamp <= current) return

        val values =
            ContentValues().apply {
                put(COL_TABLE_NAME, tableName)
                put(COL_DOWNLOADED_TILL, timestamp)
            }
        dbManager
            .getDb()
            .insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    /**
     * Reset the cursor for [tableName] to zero (re-download everything on next sync).
     * Useful during logout or full re-sync scenarios.
     */
    fun reset(tableName: String) {
        dbManager.getDb().delete(TABLE_NAME, "$COL_TABLE_NAME = ?", arrayOf(tableName))
    }

    /** Reset all download cursors. */
    fun resetAll() {
        dbManager.getDb().delete(TABLE_NAME, null, null)
    }
}
