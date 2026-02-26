package com.tejpratapsingh.motionstore.infra

import com.tejpratapsingh.motionstore.dao.DownloadedTrackerDao
import com.tejpratapsingh.motionstore.dao.SyncableDao
import com.tejpratapsingh.motionstore.domain.BackendAdapter
import com.tejpratapsingh.motionstore.domain.SyncException
import com.tejpratapsingh.motionstore.domain.SyncResult
import com.tejpratapsingh.motionstore.domain.SyncStatus
import com.tejpratapsingh.motionstore.domain.SyncableEntity
import com.tejpratapsingh.motionstore.tables.SyncTracker
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Orchestrates the full bidirectional sync cycle for one or more tables.
 *
 * Each call to [sync] (or [syncTable]) executes the following steps:
 *
 *   DOWNLOAD PHASE
 *   ─────────────
 *   1. Read `downloadedTill` cursor from [DownloadedTrackerDao] for this table.
 *   2. Fetch all server rows with `uploadedAt > downloadedTill`,
 *      excluding rows from this device (`updatedBy == deviceId`).
 *   3. For each fetched row:
 *        a. Look up a local row with the same `serverId`.
 *        b. If no local row exists → INSERT (new data from server).
 *        c. If local row exists and server's `updatedOn` ≥ local `updatedOn` → UPDATE.
 *        d. If local row exists and local `updatedOn` is newer → SKIP (local wins).
 *   4. After all rows are saved, advance `downloadedTill` to the highest
 *      `uploadedAt` seen in this batch.
 *
 *   UPLOAD PHASE
 *   ─────────────
 *   5. Query all local rows where `isDirty = 1`.
 *   6. For each dirty row:
 *        a. If `serverId == null` → POST (CREATE) to the server.
 *        b. If `serverId != null` → PUT (UPDATE) to the server.
 *   7. On success, write the returned `serverId` (creates only) and `uploadedAt`
 *      back to the local row and clear `isDirty`.
 *
 * @param backend           The [BackendAdapter] implementation to use.
 * @param downloadedTracker DAO for persisting the download cursor.
 * @param scope             CoroutineScope in which sync jobs run.
 *                          Defaults to a SupervisorJob so one table failure
 *                          does not cancel others.
 */
class SyncManager(
    private val backend: BackendAdapter,
    private val downloadedTracker: DownloadedTrackerDao,
    private val scope: CoroutineScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO + CoroutineName("SyncManager"),
        ),
) {
    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)

    /** Observable sync status. Collect in your ViewModel to drive UI. */
    val status: StateFlow<SyncStatus> = _status

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Run a full sync cycle across all [daos] concurrently.
     * Each table runs in its own coroutine under a [SupervisorJob], so a
     * failure in one table does not block the others.
     *
     * @return List of [SyncResult], one per DAO/table.
     */
    suspend fun sync(daos: List<SyncableDao<*>>): List<SyncResult> {
        _status.value = SyncStatus.Running(daos.joinToString { it.tableName })
        val results =
            daos
                .map { dao ->
                    scope.async { syncTable(dao) }
                }.awaitAll()
        _status.value = SyncStatus.Completed(results)
        return results
    }

    /**
     * Sync a single table. Safe to call directly for targeted syncs.
     */
    suspend fun <T : SyncableEntity> syncTable(dao: SyncableDao<T>): SyncResult {
        _status.value = SyncStatus.Running(dao.tableName)
        return try {
            val downloadResult = download(dao)
            val uploadResult = upload(dao)
            SyncResult(
                tableName = dao.tableName,
                downloaded = downloadResult.saved,
                conflicts = downloadResult.conflicts,
                skipped = downloadResult.skipped,
                uploaded = uploadResult.uploaded,
                uploadFailed = uploadResult.failed,
            )
        } catch (e: SyncException) {
            SyncResult(tableName = dao.tableName, error = e)
        } catch (e: Exception) {
            SyncResult(tableName = dao.tableName, error = SyncException.UnknownError(e))
        }
    }

    // ── Download ──────────────────────────────────────────────────────────────

    private suspend fun <T : SyncableEntity> download(dao: SyncableDao<T>): DownloadStats {
        val since = downloadedTracker.getDownloadedTill(dao.tableName)

        val serverRows =
            try {
                backend.fetchSince(dao.tableName, since, DeviceInfo.id)
            } catch (e: Exception) {
                throw SyncException.NetworkError("Download failed for '${dao.tableName}'", e)
            }

        var saved = 0
        var conflicts = 0
        var skipped = 0
        var highWaterMark = since

        for (row in serverRows) {
            val serverId = row[SyncTracker.COL_SERVER_ID] as? String ?: continue
            val uploadedAt = (row[SyncTracker.COL_UPLOADED_AT] as? Number)?.toLong() ?: 0L

            val serverUpdatedOn = (row[SyncTracker.COL_UPDATED_ON] as? Number)?.toLong() ?: 0L

            val localEntity = dao.findByServerId(serverId)

            when {
                // ── New row from server — never seen locally ──────────────────
                localEntity == null -> {
                    val entity =
                        try {
                            dao.fromServerRow(row)
                        } catch (e: Exception) {
                            throw SyncException.ParseError(dao.tableName, row, e)
                        }
                    dao.insert(entity)
                    saved++
                }

                // ── Conflict: server wins (server is newer or equal) ──────────
                serverUpdatedOn >= localEntity.syncTracker.updatedOn -> {
                    val entity =
                        try {
                            dao.fromServerRow(row, localId = localEntity.id)
                        } catch (e: Exception) {
                            throw SyncException.ParseError(dao.tableName, row, e)
                        }
                    dao.update(localEntity.id, entity)
                    conflicts++
                    saved++
                }

                // ── Conflict: local wins (local is newer) — skip ──────────────
                else -> {
                    skipped++
                }
            }

            if (uploadedAt > highWaterMark) highWaterMark = uploadedAt
        }

        // Advance cursor only after all rows are safely written
        if (highWaterMark > since) {
            downloadedTracker.setDownloadedTill(dao.tableName, highWaterMark)
        }

        return DownloadStats(saved, conflicts, skipped)
    }

    // ── Upload ────────────────────────────────────────────────────────────────

    private suspend fun <T : SyncableEntity> upload(dao: SyncableDao<T>): UploadStats {
        val dirtyRows = dao.findDirty()
        var uploaded = 0
        var failed = 0

        for (entity in dirtyRows) {
            try {
                val payload =
                    dao.toServerMap(entity).toMutableMap().apply {
                        put(SyncTracker.COL_UPDATED_BY, DeviceInfo.id)
                    }

                if (entity.syncTracker.serverId == null) {
                    // ── CREATE ────────────────────────────────────────────────
                    val response = backend.create(dao.tableName, payload)
                    val serverId =
                        response[SyncTracker.COL_SERVER_ID] as? String
                            ?: throw SyncException.NetworkError(
                                "Server did not return '${SyncTracker.COL_SERVER_ID}' after create on '${dao.tableName}'",
                            )
                    val uploadedAt =
                        (response[SyncTracker.COL_UPLOADED_AT] as? Number)?.toLong()
                            ?: System.currentTimeMillis()
                    dao.markUploaded(entity.id, serverId, uploadedAt)
                } else {
                    // ── UPDATE ────────────────────────────────────────────────
                    val response =
                        backend.update(
                            dao.tableName,
                            entity.syncTracker.serverId!!,
                            payload,
                        )
                    val uploadedAt =
                        (response[SyncTracker.COL_UPLOADED_AT] as? Number)?.toLong()
                            ?: System.currentTimeMillis()
                    dao.markSynced(entity.id, uploadedAt)
                }

                uploaded++
            } catch (e: SyncException) {
                // Log and continue — a single row failure must not abort the whole batch
                failed++
            } catch (e: Exception) {
                failed++
            }
        }

        return UploadStats(uploaded, failed)
    }

    // ── Internal data holders ─────────────────────────────────────────────────

    private data class DownloadStats(
        val saved: Int,
        val conflicts: Int,
        val skipped: Int,
    )

    private data class UploadStats(
        val uploaded: Int,
        val failed: Int,
    )

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /** Cancel the internal coroutine scope (call from Application.onTerminate or similar). */
    fun cancel() {
        scope.cancel()
        _status.value = SyncStatus.Idle
    }
}
