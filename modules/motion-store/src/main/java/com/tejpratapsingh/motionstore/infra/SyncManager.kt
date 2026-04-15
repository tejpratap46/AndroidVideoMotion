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
    val status: StateFlow<SyncStatus> = _status

    // Define the column name for the User ID
    companion object {
        const val COL_USER_ID = "userId"
    }

    suspend fun sync(daoList: List<SyncableDao<*>>): List<SyncResult> {
        _status.value = SyncStatus.Running(daoList.joinToString { it.tableName })
        val results =
            daoList
                .map { dao ->
                    scope.async { syncTable(dao) }
                }.awaitAll()
        _status.value = SyncStatus.Completed(results)
        return results
    }

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
                localEntity == null -> {
                    val entity = dao.fromServerRow(row)
                    dao.insert(entity)
                    saved++
                }

                serverUpdatedOn >= localEntity.syncTracker.updatedOn -> {
                    val entity = dao.fromServerRow(row, localId = localEntity.id)
                    dao.update(localEntity.id, entity)
                    conflicts++
                    saved++
                }

                else -> {
                    skipped++
                }
            }

            if (uploadedAt > highWaterMark) highWaterMark = uploadedAt
        }

        if (highWaterMark > since) {
            downloadedTracker.setDownloadedTill(dao.tableName, highWaterMark)
        }

        return DownloadStats(saved, conflicts, skipped)
    }

    private suspend fun <T : SyncableEntity> upload(dao: SyncableDao<T>): UploadStats {
        val dirtyRows = dao.findDirty()
        var uploaded = 0
        val failed = 0

        // Get the current userId once per upload batch
        val currentUserId = backend.userId

        for (entity in dirtyRows) {
            try {
                val payload =
                    dao.toServerMap(entity).toMutableMap().apply {
                        put(SyncTracker.COL_UPDATED_BY, DeviceInfo.id)
                        // If a userId is available, include it in the payload
                        currentUserId?.let { put(COL_USER_ID, it) }
                    }

                if (entity.syncTracker.serverId == null) {
                    val response = backend.create(dao.tableName, payload)
                    val serverId =
                        response[SyncTracker.COL_SERVER_ID] as? String
                            ?: throw SyncException.NetworkError("Server missing ID")
                    val uploadedAt =
                        (response[SyncTracker.COL_UPLOADED_AT] as? Number)?.toLong()
                            ?: System.currentTimeMillis()
                    dao.markUploaded(entity.id, serverId, uploadedAt)
                } else {
                    val response = backend.update(dao.tableName, entity.syncTracker.serverId!!, payload)
                    val uploadedAt =
                        (response[SyncTracker.COL_UPLOADED_AT] as? Number)?.toLong()
                            ?: System.currentTimeMillis()
                    dao.markSynced(entity.id, uploadedAt)
                }
                uploaded++
            } catch (e: Exception) {
                throw SyncException.NetworkError("Upload failed for ${dao.tableName}", e)
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
