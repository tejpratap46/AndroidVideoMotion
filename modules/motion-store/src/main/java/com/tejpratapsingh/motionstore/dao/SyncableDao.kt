package com.tejpratapsingh.motionstore.dao

import android.content.ContentValues
import android.database.Cursor
import com.tejpratapsingh.motionstore.domain.SyncableEntity
import com.tejpratapsingh.motionstore.infra.DatabaseManager
import com.tejpratapsingh.motionstore.tables.SyncTracker
import java.util.UUID

/**
 * Extends [BaseDao] for entities that implement [SyncableEntity].
 *
 * Adds sync-specific persistence operations used internally by [SyncManager]:
 *   - Reading and writing [SyncTracker] columns alongside entity columns.
 *   - Querying dirty rows for upload.
 *   - Applying server acknowledgement (serverId + uploadedAt) after a successful upload.
 *   - Conflict resolution on download.
 *
 * Concrete DAOs extend this instead of [BaseDao] and additionally implement
 * [syncTrackerFromCursor] to extract the tracker from a cursor row.
 *
 * ── Example ──────────────────────────────────────────────────────────────────
 *
 *   class UserDao(dbManager: DatabaseManager) : SyncableDao<User>(dbManager) {
 *
 *       companion object { const val SCHEMA = "CREATE TABLE IF NOT EXISTS users ( ... )" }
 *
 *       override val tableName = "users"
 *
 *       override fun toContentValues(entity: User) = ContentValues().apply {
 *           put("name",  entity.name)
 *           put("email", entity.email)
 *           putSyncTracker(entity.syncTracker)   // ← call this to persist tracker fields
 *       }
 *
 *       override fun fromCursor(cursor: Cursor) = User(
 *           id          = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
 *           name        = cursor.getString(cursor.getColumnIndexOrThrow("name")),
 *           email       = cursor.getString(cursor.getColumnIndexOrThrow("email")),
 *           syncTracker = cursor.readSyncTracker(),
 *       )
 *   }
 */
abstract class SyncableDao<T : SyncableEntity>(
    dbManager: DatabaseManager,
) : BaseDao<T>(dbManager) {
    // ── Dirty row queries ─────────────────────────────────────────────────────

    /**
     * Returns all rows where [SyncTracker.COL_IS_DIRTY] = 1.
     * Called by [SyncManager] during the upload phase.
     */
    fun findDirty(): List<T> =
        findWhere(
            whereClause = "${SyncTracker.COL_IS_DIRTY} = 1",
            whereArgs = emptyArray(),
        )

    // ── Sync-aware local insert/update ────────────────────────────────────────

    /**
     * Insert a new entity and mark it dirty. The returned ID is the SQLite rowid.
     * The SyncTracker defaults (isDirty=true, serverId=null) represent a
     * locally-created row not yet seen by the server.
     */
    fun insertLocal(entity: T): Long = insert(entity)

    /**
     * Update an existing entity and mark it dirty so it gets picked up on
     * the next upload cycle.
     */
    fun updateLocal(entity: T): Int {
        val dirtyTracker =
            entity.syncTracker.copy(
                isDirty = true,
                updatedOn = System.currentTimeMillis(),
            )
        @Suppress("UNCHECKED_CAST")
        return update(entity.id, entity.withSyncTracker(dirtyTracker) as T)
    }

    // ── Server acknowledgement ────────────────────────────────────────────────

    /**
     * After a successful server CREATE, stamp the local row with the [serverId]
     * and [uploadedAt] returned by the server, and clear the dirty flag.
     */
    fun markUploaded(
        localId: String,
        serverId: String,
        uploadedAt: Long,
    ) {
        val entity = findById(localId) ?: return
        val updatedTracker =
            entity.syncTracker.copy(
                isDirty = false,
                serverId = serverId,
                uploadedAt = uploadedAt,
            )
        @Suppress("UNCHECKED_CAST")
        update(localId, entity.withSyncTracker(updatedTracker) as T)
    }

    /**
     * After a successful server UPDATE, stamp the local row with the new
     * [uploadedAt] and clear the dirty flag.
     */
    fun markSynced(
        localId: String,
        uploadedAt: Long,
    ) {
        val entity = findById(localId) ?: return
        val updatedTracker =
            entity.syncTracker.copy(
                isDirty = false,
                uploadedAt = uploadedAt,
            )
        @Suppress("UNCHECKED_CAST")
        update(localId, entity.withSyncTracker(updatedTracker) as T)
    }

    // ── Download conflict resolution ──────────────────────────────────────────

    /**
     * Find a local row that shares the same [serverId], or null if this is
     * a new row from the server that we haven't seen before.
     */
    fun findByServerId(serverId: String): T? =
        findWhere(
            whereClause = "${SyncTracker.COL_SERVER_ID} = ?",
            whereArgs = arrayOf(serverId),
        ).firstOrNull()

    // ── ContentValues helpers (call from toContentValues) ─────────────────────

    /**
     * Extension on [ContentValues] to write all [SyncTracker] fields.
     * Call this inside your [toContentValues] implementation.
     *
     *   override fun toContentValues(entity: User) = ContentValues().apply {
     *       put("name", entity.name)
     *       putSyncTracker(entity.syncTracker)
     *   }
     */
    protected fun ContentValues.putSyncTracker(tracker: SyncTracker) {
        put(SyncTracker.COL_IS_DIRTY, if (tracker.isDirty) 1 else 0)
        put(SyncTracker.COL_UPDATED_BY, tracker.updatedBy)
        put(SyncTracker.COL_CREATED_ON, tracker.createdOn)
        put(SyncTracker.COL_UPDATED_ON, tracker.updatedOn)
        if (tracker.serverId != null) put(SyncTracker.COL_SERVER_ID, tracker.serverId)
        if (tracker.uploadedAt != null) put(SyncTracker.COL_UPLOADED_AT, tracker.uploadedAt)
    }

    /**
     * Extension on [Cursor] to reconstruct a [SyncTracker] from the current row.
     * Call this inside your [fromCursor] implementation.
     *
     *   override fun fromCursor(cursor: Cursor) = User(
     *       ...
     *       syncTracker = cursor.readSyncTracker(),
     *   )
     */
    protected fun Cursor.readSyncTracker(): SyncTracker {
        fun idx(col: String) = getColumnIndexOrThrow(col)
        return SyncTracker(
            isDirty = getInt(idx(SyncTracker.COL_IS_DIRTY)) == 1,
            updatedBy = getString(idx(SyncTracker.COL_UPDATED_BY)),
            createdOn = getLong(idx(SyncTracker.COL_CREATED_ON)),
            updatedOn = getLong(idx(SyncTracker.COL_UPDATED_ON)),
            serverId = getString(idx(SyncTracker.COL_SERVER_ID)),
            uploadedAt =
                if (isNull(idx(SyncTracker.COL_UPLOADED_AT))) {
                    null
                } else {
                    getLong(idx(SyncTracker.COL_UPLOADED_AT))
                },
        )
    }

    // ── Entity → server map ───────────────────────────────────────────────────

    /**
     * Serialize [entity] into a flat [Map] suitable for the [BackendAdapter].
     * The default implementation converts [ContentValues] to a map.
     * Override this if you need custom server field names.
     */
    open fun toServerMap(entity: T): Map<String, Any?> =
        toContentValues(entity).let { cv ->
            cv.keySet().associateWith { cv.get(it) }
        }

    // ── Server map → entity ───────────────────────────────────────────────────

    /**
     * Deserialize a raw server field map into an entity [T].
     * Called by [SyncManager] during the download phase for both inserts and
     * conflict-resolution updates.
     *
     * [localId] is the existing SQLite rowid when updating a conflicting row,
     * or -1 when inserting a brand-new row from the server.
     *
     * ── Example ──────────────────────────────────────────────────────────────
     *
     *   override fun fromServerRow(row: Map<String, Any?>, localId: Long) = User(
     *       id    = localId,
     *       name  = row["name"] as String,
     *       email = row["email"] as String,
     *       syncTracker = SyncTracker(
     *           isDirty    = false,
     *           updatedBy  = row[SyncTracker.COL_UPDATED_BY] as String,
     *           createdOn  = (row[SyncTracker.COL_CREATED_ON] as Number).toLong(),
     *           updatedOn  = (row[SyncTracker.COL_UPDATED_ON] as Number).toLong(),
     *           serverId   = row[SyncTracker.COL_SERVER_ID] as? String,
     *           uploadedAt = (row[SyncTracker.COL_UPLOADED_AT] as? Number)?.toLong(),
     *       ),
     *   )
     */
    abstract fun fromServerRow(
        row: Map<String, Any?>,
        localId: String = UUID.randomUUID().toString(),
    ): T
}
