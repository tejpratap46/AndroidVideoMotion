package com.tejpratapsingh.motionstore.infra

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tejpratapsingh.motionstore.domain.BackendAdapter
import com.tejpratapsingh.motionstore.tables.SyncTracker
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * [BackendAdapter] backed by Cloud Firestore.
 *
 * Each [tableName] maps to a Firestore collection of the same name.
 * Server-side filtering (exclude own deviceId, filter by uploadedAt) is
 * done via Firestore query operators — no extra Cloud Functions needed.
 *
 * Requires:  com.google.firebase:firebase-firestore-ktx
 *            org.jetbrains.kotlinx:kotlinx-coroutines-play-services
 *
 * Note: Firestore does not support `uploadedAt` being set server-side
 * automatically unless you use a Cloud Function trigger. If you want true
 * server timestamps, replace the client-supplied uploadedAt with
 * FieldValue.serverTimestamp() in [create] and [update] and read it back
 * via a subsequent get(). The simpler client-timestamp approach is used
 * here for portability.
 */
class FirebaseAdapter(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : BackendAdapter {
    override val userId: String?
        get() =
            com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid

    fun convertNanosToTimestamp(totalNanos: Long): Timestamp {
        val seconds = totalNanos / 1_000_000_000L
        val remainingNanos = (totalNanos % 1_000_000_000L).toInt()
        return Timestamp(seconds, remainingNanos)
    }

    override suspend fun fetchSince(
        tableName: String,
        since: Long,
        deviceId: String,
    ): List<Map<String, Any?>> =
        try {
            val sinceTimestamp = convertNanosToTimestamp(since)
            Timber.d("Firestore: fetchSince(table=$tableName, since=$since ($sinceTimestamp), deviceId=$deviceId)")
            val snapshot =
                firestore
                    .collection(tableName)
                    .whereGreaterThan(SyncTracker.COL_UPLOADED_AT, sinceTimestamp)
                    .whereNotEqualTo(SyncTracker.COL_UPDATED_BY, deviceId)
                    .orderBy(SyncTracker.COL_UPDATED_BY)
                    .orderBy(SyncTracker.COL_UPLOADED_AT, Query.Direction.ASCENDING)
                    .get()
                    .await()

            Timber.d("Firestore: fetchSince(table=$tableName) returned ${snapshot.size()} documents")
            snapshot.documents.map { doc ->
                doc.data.orEmpty().toMutableMap().apply {
                    // Expose Firestore document ID as serverId for consistency
                    put(SyncTracker.COL_SERVER_ID, doc.id)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Firestore: fetchSince(table=$tableName) failed")
            emptyList()
        }

    override suspend fun create(
        tableName: String,
        data: Map<String, Any?>,
    ): Map<String, Any?> =
        try {
            Timber.d("Firestore: create(table=$tableName, data=$data)")
            val payload =
                data
                    .toMutableMap()
                    .apply { put(SyncTracker.COL_UPLOADED_AT, FieldValue.serverTimestamp()) }

            val docRef = firestore.collection(tableName).add(payload).await()
            Timber.d("Firestore: Document created with ID: ${docRef.id} in $tableName")

            // Read the created doc back so we return the full server state
            val created = docRef.get().await()
            val result = created.data.orEmpty().toMutableMap().apply {
                put(SyncTracker.COL_SERVER_ID, docRef.id)
                put(
                    SyncTracker.COL_UPLOADED_AT,
                    created.getTimestamp(SyncTracker.COL_UPLOADED_AT)?.nanoseconds,
                )
            }
            Timber.v("Firestore: create(table=$tableName) returning result: $result")
            result
        } catch (e: Exception) {
            Timber.e(e, "Firestore: create(table=$tableName) failed")
            data // Return original data on failure or handle as needed
        }

    override suspend fun update(
        tableName: String,
        serverId: String,
        data: Map<String, Any?>,
    ): Map<String, Any?> =
        try {
            Timber.d("Firestore: update(table=$tableName, serverId=$serverId, data=$data)")
            val payload =
                data
                    .toMutableMap()
                    .apply { put(SyncTracker.COL_UPLOADED_AT, FieldValue.serverTimestamp()) }

            firestore
                .collection(tableName)
                .document(serverId)
                .set(payload)
                .await()
            Timber.d("Firestore: Document $serverId updated in $tableName")

            val docRef = firestore.collection(tableName).document(serverId)
            val updatedDoc = docRef.get().await()

            val result = payload.apply {
                put(SyncTracker.COL_SERVER_ID, serverId)
                put(
                    SyncTracker.COL_UPLOADED_AT,
                    updatedDoc.getTimestamp(SyncTracker.COL_UPLOADED_AT)?.nanoseconds,
                )
            }
            Timber.v("Firestore: update(table=$tableName) returning result: $result")
            result
        } catch (e: Exception) {
            Timber.e(e, "Firestore: update(table=$tableName, serverId=$serverId) failed")
            data
        }

    override suspend fun delete(
        tableName: String,
        serverId: String,
    ) {
        try {
            Timber.d("Firestore: delete(table=$tableName, serverId=$serverId)")
            firestore
                .collection(tableName)
                .document(serverId)
                .delete()
                .await()
            Timber.d("Firestore: Document $serverId deleted from $tableName")
        } catch (e: Exception) {
            Timber.e(e, "Firestore: delete(table=$tableName, serverId=$serverId) failed")
        }
    }
}
