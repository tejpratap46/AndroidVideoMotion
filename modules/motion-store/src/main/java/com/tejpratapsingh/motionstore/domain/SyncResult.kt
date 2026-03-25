package com.tejpratapsingh.motionstore.domain

/**
 * Summary of a completed sync cycle returned by [SyncManager.sync].
 */
data class SyncResult(
    val tableName: String,
    val downloaded: Int = 0,
    val conflicts: Int = 0,
    val skipped: Int = 0,
    val uploaded: Int = 0,
    val uploadFailed: Int = 0,
    val error: SyncException? = null,
) {
    val hasError: Boolean get() = error != null

    override fun toString(): String =
        "[$tableName] ↓$downloaded conflicts=$conflicts skipped=$skipped ↑$uploaded failed=$uploadFailed" +
            if (hasError) " ERROR=${error!!.message}" else ""
}

/** Emitted by [SyncManager] so observers can react to sync lifecycle events. */
sealed class SyncStatus {
    object Idle : SyncStatus()

    data class Running(
        val tableName: String,
    ) : SyncStatus()

    data class Completed(
        val results: List<SyncResult>,
    ) : SyncStatus()

    data class Failed(
        val error: SyncException,
    ) : SyncStatus()
}
