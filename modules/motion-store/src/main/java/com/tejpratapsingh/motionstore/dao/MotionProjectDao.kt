package com.tejpratapsingh.motionstore.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.tejpratapsingh.motionstore.tables.MotionProject

class MotionProjectDao(
    private val db: SQLiteDatabase,
) {
    companion object {
        const val TABLE = "MotionProject"

        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_PATH = "path"
        const val COL_SDUI = "sdui"
        const val COL_CREATED = "created"
        const val COL_UPDATED = "updated"

        const val CREATE_TABLE = """
            CREATE TABLE $TABLE (
                $COL_ID STRING NOT NULL PRIMARY KEY,
                $COL_NAME TEXT NOT NULL,
                $COL_PATH TEXT NOT NULL,
                $COL_SDUI TEXT,
                $COL_CREATED INTEGER NOT NULL,
                $COL_UPDATED INTEGER NOT NULL
            )
        """
    }

    // CREATE
    fun insert(project: MotionProject): Long {
        val values = project.toContentValues()
        return db.insertOrThrow(TABLE, null, values)
    }

    // READ — single
    fun getById(id: String): MotionProject? {
        val cursor =
            db.query(
                TABLE,
                null,
                "$COL_ID = ?",
                arrayOf(id),
                null,
                null,
                null,
            )
        return cursor.use { if (it.moveToFirst()) it.toMotionProject() else null }
    }

    // READ — all
    fun getAll(): List<MotionProject> {
        val cursor = db.query(TABLE, null, null, null, null, null, "$COL_UPDATED DESC")
        return cursor.use { c ->
            buildList {
                while (c.moveToNext()) add(c.toMotionProject())
            }
        }
    }

    // UPDATE
    fun update(project: MotionProject): Int {
        val values =
            project.toContentValues().apply {
                put(COL_UPDATED, System.currentTimeMillis())
            }
        return db.update(TABLE, values, "$COL_ID = ?", arrayOf(project.id))
    }

    // DELETE
    fun delete(id: Long): Int = db.delete(TABLE, "$COL_ID = ?", arrayOf(id.toString()))

    fun deleteAll(): Int = db.delete(TABLE, null, null)

    // --- Helpers ---

    private fun MotionProject.toContentValues() =
        ContentValues().apply {
            put(COL_NAME, name)
            put(COL_PATH, path)
            put(COL_SDUI, sdui)
            put(COL_CREATED, created)
            put(COL_UPDATED, updated)
        }

    private fun Cursor.toMotionProject() =
        MotionProject(
            id = getString(getColumnIndexOrThrow(COL_ID)),
            name = getString(getColumnIndexOrThrow(COL_NAME)),
            path = getString(getColumnIndexOrThrow(COL_PATH)),
            sdui = getString(getColumnIndexOrThrow(COL_SDUI)),
            created = getLong(getColumnIndexOrThrow(COL_CREATED)),
            updated = getLong(getColumnIndexOrThrow(COL_UPDATED)),
        )
}
