package com.tejpratapsingh.motionstore.tables

/**
 * Sync metadata attached to every syncable row.
 *
 * Stored as extra columns on the entity's own table — NOT a separate table —
 * so reads are cheap and atomic with the entity itself.
 *
 * Column names are exposed as constants so DAOs can reference them
 * without magic strings.
 */
data class SyncTracker(
    /** True whenever the local row is ahead of the server. Set on every local write. */
    val isDirty: Boolean = true,
    /** Device ID of the device that last modified this row locally. */
    val updatedBy: String,
    /** Local timestamp (epoch ms) when the row was first created on this device. */
    val createdOn: Long = System.currentTimeMillis(),
    /** Local timestamp (epoch ms) of the last local modification. */
    val updatedOn: Long = System.currentTimeMillis(),
    /**
     * ID assigned by the server when the row was first uploaded.
     * Null until the server has acknowledged the creation.
     * Used to distinguish CREATE vs UPDATE when uploading dirty rows.
     */
    val serverId: String? = null,
    /**
     * Server-side timestamp (epoch ms) written by the server on every write.
     * Used as the cursor for incremental downloads.
     * Null until the server has written the row at least once.
     */
    val uploadedAt: Long? = null,
) {
    companion object {
        // ── Column name constants ────────────────────────────────────────────
        const val COL_IS_DIRTY = "is_dirty"
        const val COL_UPDATED_BY = "updated_by"
        const val COL_CREATED_ON = "created_on"
        const val COL_UPDATED_ON = "updated_on"
        const val COL_SERVER_ID = "server_id"
        const val COL_UPLOADED_AT = "uploaded_at"

        /**
         * SQL fragment to append to any CREATE TABLE statement.
         *
         * Usage:
         *   "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT, ${SyncTracker.COLUMNS_SQL})"
         */
        const val COLUMNS_SQL = """
            $COL_IS_DIRTY    INTEGER NOT NULL DEFAULT 1,
            $COL_UPDATED_BY  TEXT    NOT NULL,
            $COL_CREATED_ON  INTEGER NOT NULL,
            $COL_UPDATED_ON  INTEGER NOT NULL,
            $COL_SERVER_ID   TEXT,
            $COL_UPLOADED_AT INTEGER
        """
    }
}
