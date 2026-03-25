package com.tejpratapsingh.motionstore.domain

/**
 * Abstraction layer over the remote backend.
 *
 * Implement this interface once per backend (Firebase, Supabase, REST).
 * [SyncManager] only ever calls these four methods — swapping backends
 * means swapping the [BackendAdapter] implementation, nothing else.
 *
 * All methods are suspend functions; call them from a coroutine scope.
 *
 * ── Type parameter ───────────────────────────────────────────────────────────
 * [T] is the DTO / map type that the backend speaks. Each concrete adapter
 * converts between [T] and [Map<String, Any?>] internally, so [SyncManager]
 * always works with plain maps.
 */
interface BackendAdapter {
    /**
     * Fetch rows from [tableName] on the server where `uploadedAt > since`.
     * The server must exclude rows whose `updatedBy == deviceId` so a device
     * never downloads its own uploads back.
     *
     * @param tableName  Remote collection / table name.
     * @param since      Epoch-ms cursor. Pass 0 to fetch everything.
     * @param deviceId   The calling device's ID. Server must filter this out.
     * @return List of rows as raw field maps, including all sync fields.
     */
    suspend fun fetchSince(
        tableName: String,
        since: Long,
        deviceId: String,
    ): List<Map<String, Any?>>

    /**
     * Create a new row on the server.
     * The server generates and returns a [serverId] and an [uploadedAt] timestamp.
     *
     * @param tableName Remote collection / table name.
     * @param data      Entity fields + sync tracker fields (without serverId / uploadedAt).
     * @return The server's response map, which must include
     *                  [SyncTracker.COL_SERVER_ID] and [SyncTracker.COL_UPLOADED_AT].
     */
    suspend fun create(
        tableName: String,
        data: Map<String, Any?>,
    ): Map<String, Any?>

    /**
     * Update an existing row on the server identified by [serverId].
     * The server updates and returns the new [uploadedAt] timestamp.
     *
     * @param tableName Remote collection / table name.
     * @param serverId  The server-side row identifier.
     * @param data      Updated entity fields + sync tracker fields.
     * @return The server's response map, which must include
     *                  [SyncTracker.COL_UPLOADED_AT].
     */
    suspend fun update(
        tableName: String,
        serverId: String,
        data: Map<String, Any?>,
    ): Map<String, Any?>

    /**
     * Delete a row on the server. Optional — implement as a no-op if your
     * backend uses soft deletes instead.
     *
     * @param tableName Remote collection / table name.
     * @param serverId  The server-side row identifier.
     */
    suspend fun delete(
        tableName: String,
        serverId: String,
    )
}
