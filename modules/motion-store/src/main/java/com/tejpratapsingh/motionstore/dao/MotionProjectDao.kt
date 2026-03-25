package com.tejpratapsingh.motionstore.dao

import android.content.ContentValues
import android.database.Cursor
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.tejpratapsingh.motionstore.infra.DatabaseManager
import com.tejpratapsingh.motionstore.tables.MotionProject
import com.tejpratapsingh.motionstore.tables.SyncTracker

class MotionProjectDao(
    dbManager: DatabaseManager,
) : SyncableDao<MotionProject>(dbManager) {
    companion object {
        const val TABLE = "MotionProject"

        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_PATH = "path"
        const val COL_SDUI = "sdui"
        const val COL_METADATA = "metadata"
        const val COL_CREATED = "created"
        const val COL_UPDATED = "updated"

        const val SCHEMA = """
            CREATE TABLE $TABLE (
                $COL_ID STRING NOT NULL PRIMARY KEY,
                $COL_NAME TEXT NOT NULL,
                $COL_PATH TEXT NOT NULL,
                $COL_SDUI TEXT,
                $COL_METADATA TEXT,
                $COL_CREATED INTEGER NOT NULL,
                $COL_UPDATED INTEGER NOT NULL,
                ${SyncTracker.COLUMNS_SQL}
            )
        """
    }

    override val tableName = TABLE

    override val primaryKey: String
        get() = COL_ID

    override fun fromCursor(cursor: Cursor): MotionProject =
        MotionProject(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
            path = cursor.getString(cursor.getColumnIndexOrThrow(COL_PATH)),
            sdui = cursor.getString(cursor.getColumnIndexOrThrow(COL_SDUI))?.toJsonObject() ?: JsonObject(),
            metadata = cursor.getString(cursor.getColumnIndexOrThrow(COL_METADATA))?.toJsonObject() ?: JsonObject(),
            created = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED)),
            updated = cursor.getLong(cursor.getColumnIndexOrThrow(COL_UPDATED)),
            syncTracker = cursor.readSyncTracker(),
        )

    override fun toContentValues(entity: MotionProject): ContentValues =
        ContentValues().apply {
            put(COL_ID, entity.id)
            put(COL_NAME, entity.name)
            put(COL_PATH, entity.path)
            put(COL_SDUI, entity.sdui.toString())
            put(COL_METADATA, entity.metadata.toString())
            put(COL_CREATED, entity.created)
            put(COL_UPDATED, entity.updated)
            putSyncTracker(entity.syncTracker)
        }

    override fun fromServerRow(
        row: Map<String, Any?>,
        localId: String,
    ) = MotionProject(
        id = localId,
        name = row[COL_NAME] as String,
        path = row[COL_PATH] as String,
        sdui = (row[COL_SDUI] as? String)?.toJsonObject() ?: JsonObject(),
        metadata = (row[COL_METADATA] as? String)?.toJsonObject() ?: JsonObject(),
        created = (row[COL_CREATED] as Number).toLong(),
        updated = (row[COL_UPDATED] as Number).toLong(),
        syncTracker =
            SyncTracker(
                isDirty = false,
                updatedBy = row[SyncTracker.COL_UPDATED_BY] as String,
                createdOn = (row[SyncTracker.COL_CREATED_ON] as Number).toLong(),
                updatedOn = (row[SyncTracker.COL_UPDATED_ON] as Number).toLong(),
                serverId = row[SyncTracker.COL_SERVER_ID] as? String,
                uploadedAt = (row[SyncTracker.COL_UPLOADED_AT] as? Number)?.toLong(),
            ),
    )

    private fun String.toJsonObject(): JsonObject {
        return try {
            JsonParser.parseString(this).asJsonObject
        } catch (e: Exception) {
            JsonObject()
        }
    }
}
