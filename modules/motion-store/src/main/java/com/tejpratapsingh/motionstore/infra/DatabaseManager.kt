package com.tejpratapsingh.motionstore.infra

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.tejpratapsingh.motionstore.dao.DownloadedTrackerDao
import com.tejpratapsingh.motionstore.dao.MotionProjectDao
import com.tejpratapsingh.motionstore.infra.DatabaseManager.Companion.init

/**
 * Singleton that owns the [SQLiteOpenHelper] lifecycle and vends
 * a shared [SQLiteDatabase] instance to all DAOs.
 *
 * It accepts plain SQL strings for table creation, so it has zero
 * dependency on any DAO — breaking the chicken-and-egg problem.
 *
 * ── Setup in Application.onCreate() ─────────────────────────────────────────
 *
 *   DatabaseManager.init(
 *       context      = this,
 *       databaseName = "app.db",
 *       version      = 1,
 *       schemas      = listOf(UserDao.SCHEMA, PostDao.SCHEMA),
 *   )
 *
 *   // Now DAOs can safely be created and used
 *   val userDao = UserDao(DatabaseManager.getInstance())
 */
class DatabaseManager private constructor(
    context: Context,
    databaseName: String,
    databaseVersion: Int,
    private val schemas: List<String>,
    private val onUpgradeCallback: ((SQLiteDatabase, Int, Int) -> Unit)?,
) : SQLiteOpenHelper(context.applicationContext, databaseName, null, databaseVersion) {
    override fun onCreate(db: SQLiteDatabase) {
        schemas.forEach { db.execSQL(it) }
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        if (onUpgradeCallback != null) {
            onUpgradeCallback.invoke(db, oldVersion, newVersion)
        } else {
            // Default: drop all tables and recreate
            // Disable FK constraints during migration to avoid constraint errors on drop
            db.execSQL("PRAGMA foreign_keys = OFF")
            schemas.forEach { schema ->
                val tableName =
                    schema
                        .substringAfter("CREATE TABLE IF NOT EXISTS ", "")
                        .substringAfter("CREATE TABLE ", "")
                        .substringBefore(" ")
                        .trim()
                if (tableName.isNotEmpty()) db.execSQL("DROP TABLE IF EXISTS $tableName")
            }
            db.execSQL("PRAGMA foreign_keys = ON")
            onCreate(db)
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    /**
     * Returns the shared writable [SQLiteDatabase].
     * DAOs use this — do NOT call close() on the returned instance.
     */
    fun getDb(): SQLiteDatabase = writableDatabase

    companion object {
        @Volatile private var instance: DatabaseManager? = null

        /**
         * Initialize the singleton. Must be called before any DAO is used.
         *
         * @param context        Application context.
         * @param databaseName   SQLite file name (e.g. "app.db").
         * @param version        Schema version. Increment to trigger [onUpgrade].
         * @param schemas        List of raw CREATE TABLE SQL strings, one per table.
         *                       Collect these from companion objects on each DAO.
         * @param onUpgrade      Optional migration block. Receives the db and
         *                       old/new version numbers. If null, all tables are
         *                       dropped and recreated (dev-friendly default).
         */
        fun init(
            context: Context,
            databaseName: String = "app.db",
            version: Int = 1,
            schemas: List<String> = listOf(MotionProjectDao.SCHEMA, DownloadedTrackerDao.SCHEMA),
            onUpgrade: ((db: SQLiteDatabase, oldVersion: Int, newVersion: Int) -> Unit)? = null,
        ): DatabaseManager =
            instance ?: synchronized(this) {
                instance ?: DatabaseManager(context, databaseName, version, schemas, onUpgrade)
                    .also { instance = it }
            }

        /**
         * Returns the initialized singleton.
         * @throws IllegalStateException if [init] has not been called yet.
         */
        fun getInstance(): DatabaseManager =
            instance ?: error("DatabaseManager is not initialized. Call DatabaseManager.init() in Application.onCreate() first.")
    }
}
