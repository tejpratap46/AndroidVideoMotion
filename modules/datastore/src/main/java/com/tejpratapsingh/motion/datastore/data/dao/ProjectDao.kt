package com.tejpratapsingh.motion.datastore.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tejpratapsingh.motion.datastore.data.entity.ProjectEntity

@Dao
interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(projectEntity: ProjectEntity)

    @Query("SELECT * FROM projects")
    suspend fun getAll(): List<ProjectEntity>
}