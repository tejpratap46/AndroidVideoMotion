package com.tejpratapsingh.motionstore.infra

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.tejpratapsingh.motionstore.dao.MotionProjectDao

class AppDatabaseHelper(
    context: Context,
) : SQLiteOpenHelper(context, "app.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(MotionProjectDao.CREATE_TABLE)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        db.execSQL("DROP TABLE IF EXISTS ${MotionProjectDao.TABLE}")
        onCreate(db)
    }
}
