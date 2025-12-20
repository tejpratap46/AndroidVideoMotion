package com.tejpratapsingh.motion.datastore.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tejpratapsingh.motion.datastore.data.dao.ProjectDao
import com.tejpratapsingh.motion.datastore.data.entity.ProjectEntity

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = false)
abstract class ProjectDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}