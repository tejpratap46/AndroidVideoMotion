package com.tejpratapsingh.motionstore.infra

import com.tejpratapsingh.motionstore.domain.BackendAdapter
import com.tejpratapsingh.motionstore.tables.SyncTracker
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

/**
 * [BackendAdapter] backed by Supabase (PostgREST).
 *
 * Each [tableName] maps to a Supabase / Postgres table of the same name.
 * Filtering is done via PostgREST query parameters — the server handles
 * the `uploadedAt > since` and `updatedBy != deviceId` predicates natively.
 *
 * Requires:  io.github.jan-tennert.supabase:postgrest-kt
 *
 * Your Postgres tables must have RLS policies that allow the client to
 * insert / update rows. A recommended pattern is to enable RLS and add
 * a permissive policy for authenticated users.
 *
 * The `uploaded_at` column should be set server-side via a trigger:
 *   CREATE OR REPLACE FUNCTION set_uploaded_at()
 *   RETURNS TRIGGER AS $$
 *   BEGIN NEW.uploaded_at = (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT; RETURN NEW; END;
 *   $$ LANGUAGE plpgsql;
 *
 *   CREATE TRIGGER trg_uploaded_at BEFORE INSERT OR UPDATE ON <table>
 *   FOR EACH ROW EXECUTE FUNCTION set_uploaded_at();
 */
class SupabaseAdapter(
    private val client: SupabaseClient,
) : BackendAdapter {
    override val userId: String?
        get() =
            client.auth
                .currentSessionOrNull()
                ?.user
                ?.id

    override suspend fun fetchSince(
        tableName: String,
        since: Long,
        deviceId: String,
    ): List<Map<String, Any?>> {
        val result =
            client.postgrest[tableName]
                .select(Columns.ALL) {
                    filter {
                        gt(SyncTracker.COL_UPLOADED_AT, since)
                        neq(SyncTracker.COL_UPDATED_BY, deviceId)
                    }
                    order(SyncTracker.COL_UPLOADED_AT, Order.ASCENDING)
                }

        return result.data.parseJsonToListOfMaps()
    }

    override suspend fun create(
        tableName: String,
        data: Map<String, Any?>,
    ): Map<String, Any?> {
        val result =
            client.postgrest[tableName]
                .insert(data.toJsonObject()) { select() }

        return result.data.parseJsonToListOfMaps().firstOrNull()
            ?: throw IllegalStateException("Server returned empty response after insert on '$tableName'")
    }

    override suspend fun update(
        tableName: String,
        serverId: String,
        data: Map<String, Any?>,
    ): Map<String, Any?> {
        val result =
            client.postgrest[tableName]
                .update(data.toJsonObject()) {
                    filter { eq(SyncTracker.COL_SERVER_ID, serverId) }
                    select()
                }

        return result.data.parseJsonToListOfMaps().firstOrNull()
            ?: throw IllegalStateException("Supabase did not return updated row for '$tableName'")
    }

    override suspend fun delete(
        tableName: String,
        serverId: String,
    ) {
        client.postgrest[tableName].delete {
            filter { eq(SyncTracker.COL_SERVER_ID, serverId) }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun Map<String, Any?>.toJsonObject(): JsonObject =
        buildJsonObject {
            forEach { (key, value) ->
                put(key, value.toJsonElement())
            }
        }

    private fun Any?.toJsonElement(): JsonElement =
        when (this) {
            null -> {
                JsonNull
            }

            is Boolean -> {
                JsonPrimitive(this)
            }

            is Number -> {
                JsonPrimitive(this)
            }

            is String -> {
                JsonPrimitive(this)
            }

            is Map<*, *> -> {
                buildJsonObject {
                    @Suppress("UNCHECKED_CAST")
                    (this@toJsonElement as Map<String, Any?>).forEach { (k, v) ->
                        put(
                            k,
                            v.toJsonElement(),
                        )
                    }
                }
            }

            is List<*> -> {
                buildJsonArray { forEach { add(it.toJsonElement()) } }
            }

            else -> {
                JsonPrimitive(toString())
            }
        }

    private fun String.parseJsonToListOfMaps(): List<Map<String, Any?>> {
        val array = Json.parseToJsonElement(this).jsonArray
        return array.map { element ->
            element.jsonObject.entries.associate { (k, v) -> k to v.toAny() }
        }
    }

    private fun JsonElement.toAny(): Any? =
        when (this) {
            is JsonNull -> {
                null
            }

            is JsonPrimitive -> {
                when {
                    isString -> content
                    booleanOrNull != null -> boolean
                    longOrNull != null -> long
                    else -> double
                }
            }

            is JsonObject -> {
                entries.associate { (k, v) -> k to v.toAny() }
            }

            is JsonArray -> {
                map { it.toAny() }
            }
        }
}
