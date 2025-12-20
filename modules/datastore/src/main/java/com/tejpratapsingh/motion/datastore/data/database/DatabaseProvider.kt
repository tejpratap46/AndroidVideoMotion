package com.tejpratapsingh.motion.datastore.data.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: ProjectDatabase? = null

    fun getDatabase(context: Context): ProjectDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                ProjectDatabase::class.java,
                "LyricMaker"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}