package com.tejpratapsingh.motionstore.domain

import android.content.ContentValues
import com.tejpratapsingh.motionstore.tables.SyncTracker

/**
 * Contract that every entity participating in sync must satisfy.
 *
 * Each entity owns its local [id] (SQLite rowid) and carries a [syncTracker]
 * that holds all sync metadata. The framework only ever touches [syncTracker];
 * it never inspects the entity's own domain fields.
 *
 * ── Example implementation ───────────────────────────────────────────────────
 *
 *   data class User(
 *       val id: Long = -1,
 *       val name: String,
 *       val email: String,
 *       override val syncTracker: SyncTracker = SyncTracker(updatedBy = DeviceInfo.id),
 *   ) : SyncableEntity {
 *       override fun withSyncTracker(tracker: SyncTracker) = copy(syncTracker = tracker)
 *   }
 */
interface SyncableEntity {
    /** Local SQLite primary key. -1 means not yet persisted. */
    val id: String

    /** Sync metadata attached to this row. */
    val syncTracker: SyncTracker

    /**
     * Return a copy of this entity with [syncTracker] replaced.
     * Implemented via data class [copy] in every concrete entity.
     */
    fun withSyncTracker(tracker: SyncTracker): SyncableEntity
}
