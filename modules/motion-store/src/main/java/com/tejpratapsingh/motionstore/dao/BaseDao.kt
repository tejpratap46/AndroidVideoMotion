package com.tejpratapsingh.motionstore.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.sqlite.transaction
import com.tejpratapsingh.motionstore.infra.DatabaseManager

/**
 * Abstract base class for SQLite CRUD operations.
 *
 * Receives a [DatabaseManager] to obtain the shared [SQLiteDatabase].
 * It never opens or closes the database itself.
 *
 * Table creation SQL lives in each DAO's companion object so it can be
 * passed to [DatabaseManager.init] independently — no circular dependency.
 *
 * ── How to implement ─────────────────────────────────────────────────────────
 *
 *   data class User(val id: Long = -1, val name: String, val email: String)
 *
 *   class UserDao(dbManager: DatabaseManager) : BaseDao<User>(dbManager) {
 *
 *       // Companion holds the schema so DatabaseManager can be initialized
 *       // before any UserDao instance is created.
 *       companion object {
 *           const val SCHEMA = """
 *               CREATE TABLE IF NOT EXISTS users (
 *                   id    INTEGER PRIMARY KEY AUTOINCREMENT,
 *                   name  TEXT NOT NULL,
 *                   email TEXT NOT NULL UNIQUE
 *               )
 *           """
 *       }
 *
 *       override val tableName = "users"
 *
 *       override fun toContentValues(entity: User) = ContentValues().apply {
 *           put("name",  entity.name)
 *           put("email", entity.email)
 *       }
 *
 *       override fun fromCursor(cursor: Cursor) = User(
 *           id    = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
 *           name  = cursor.getString(cursor.getColumnIndexOrThrow("name")),
 *           email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
 *       )
 *   }
 *
 * ── Wiring in Application.onCreate() ────────────────────────────────────────
 *
 *   // 1. Init DatabaseManager with just the SQL schemas — no DAOs needed
 *   DatabaseManager.init(
 *       context      = this,
 *       databaseName = "app.db",
 *       version      = 1,
 *       schemas      = listOf(UserDao.SCHEMA, PostDao.SCHEMA),
 *   )
 *
 *   // 2. Now safely create DAOs
 *   val userDao = UserDao(DatabaseManager.getInstance())
 *   val postDao = PostDao(DatabaseManager.getInstance())
 */
abstract class BaseDao<T>(
    private val dbManager: DatabaseManager,
) {
    // ── Abstract contract ────────────────────────────────────────────────────

    /** Name of the SQLite table this DAO manages. */
    abstract val tableName: String

    /** Primary key column name. Override if it differs from "id". */
    open val primaryKey: String get() = "id"

    /** Map an entity to [ContentValues] for insert / update. */
    abstract fun toContentValues(entity: T): ContentValues

    /** Reconstruct an entity from the current [Cursor] row. */
    abstract fun fromCursor(cursor: Cursor): T

    // ── DB handle ────────────────────────────────────────────────────────────

    private val db: SQLiteDatabase get() = dbManager.getDb()

    // ── Create ───────────────────────────────────────────────────────────────

    /** Insert a single entity. Returns the new row ID, or -1 on failure. */
    fun insert(entity: T): Long = db.insertOrThrow(tableName, null, toContentValues(entity))

    /**
     * Insert multiple entities in a single transaction.
     * @return List of inserted row IDs in the same order as [entities].
     */
    fun insertAll(entities: List<T>): List<Long> {
        val ids = mutableListOf<Long>()
        db.transaction {
            try {
                entities.forEach { ids += insertOrThrow(tableName, null, toContentValues(it)) }
            } finally {
            }
        }
        return ids
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    /** Fetch a single entity by primary key, or null if not found. */
    fun findById(id: String): T? =
        db
            .query(
                tableName,
                null,
                "$primaryKey = ?",
                arrayOf(id),
                null,
                null,
                null,
                "1",
            ).use { if (it.moveToFirst()) fromCursor(it) else null }

    /** Fetch all rows, optionally sorted by [orderBy] (e.g. "name ASC"). */
    fun findAll(orderBy: String? = null): List<T> =
        db
            .query(tableName, null, null, null, null, null, orderBy)
            .use { it.toEntityList() }

    /**
     * Fetch rows matching a WHERE clause.
     * Example: findWhere("email = ?", arrayOf("alice@example.com"))
     */
    fun findWhere(
        whereClause: String,
        whereArgs: Array<String>,
        orderBy: String? = null,
    ): List<T> =
        db
            .query(tableName, null, whereClause, whereArgs, null, null, orderBy)
            .use { it.toEntityList() }

    /**
     * Execute a raw SELECT query and map results to entities.
     * Use this for JOINs or anything [findWhere] can't express.
     */
    fun rawQuery(
        sql: String,
        selectionArgs: Array<String>? = null,
    ): List<T> = db.rawQuery(sql, selectionArgs).use { it.toEntityList() }

    // ── Update ───────────────────────────────────────────────────────────────

    /** Update the row with [id]. Returns number of rows affected. */
    fun update(
        id: String,
        entity: T,
    ): Int = db.update(tableName, toContentValues(entity), "$primaryKey = ?", arrayOf(id.toString()))

    /**
     * Insert if no conflict exists, replace otherwise (CONFLICT_REPLACE).
     * @return The row ID of the upserted row, or -1 on failure.
     */
    fun upsert(entity: T): Long =
        db.insertWithOnConflict(
            tableName,
            null,
            toContentValues(entity),
            SQLiteDatabase.CONFLICT_REPLACE,
        )

    // ── Delete ───────────────────────────────────────────────────────────────

    /** Delete the row with the given [id]. Returns number of rows deleted. */
    fun deleteById(id: String): Int = db.delete(tableName, "$primaryKey = ?", arrayOf(id.toString()))

    /**
     * Delete rows matching [whereClause], or ALL rows if whereClause is null.
     * @return Number of rows deleted.
     */
    fun delete(
        whereClause: String? = null,
        whereArgs: Array<String>? = null,
    ): Int = db.delete(tableName, whereClause, whereArgs)

    // ── Utility ──────────────────────────────────────────────────────────────

    /** Total row count in this table. */
    fun count(): Long = db.compileStatement("SELECT COUNT(*) FROM $tableName").simpleQueryForLong()

    /** Returns true if a row with the given [id] exists. */
    fun exists(id: String): Boolean =
        db
            .compileStatement("SELECT COUNT(*) FROM $tableName WHERE $primaryKey = ?")
            .apply { bindString(1, id) }
            .simpleQueryForLong() > 0

    /**
     * Run [block] inside a single transaction.
     * Automatically rolls back if an exception is thrown.
     *
     *   userDao.withTransaction {
     *       insert(alice)
     *       insert(bob)
     *   }
     */
    fun withTransaction(block: BaseDao<T>.() -> Unit) {
        db.transaction {
            block()
        }
    }

    private fun Cursor.toEntityList(): List<T> =
        buildList {
            if (moveToFirst()) {
                do {
                    add(fromCursor(this@toEntityList))
                } while (moveToNext())
            }
        }
}
