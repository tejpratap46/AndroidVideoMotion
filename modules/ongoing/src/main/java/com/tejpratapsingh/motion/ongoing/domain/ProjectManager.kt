package com.tejpratapsingh.motion.ongoing.domain

import android.content.Context
import android.util.Log
import com.tejpratapsingh.motion.datastore.data.dao.ProjectDao
import com.tejpratapsingh.motion.datastore.data.database.DatabaseProvider
import com.tejpratapsingh.motion.datastore.data.database.ProjectDatabase
import com.tejpratapsingh.motion.ongoing.domain.mapper.toCurrentProject
import com.tejpratapsingh.motion.ongoing.domain.mapper.toProjectEntity

object ProjectManager {

    private var projectDatabase: ProjectDatabase?=null
    private var projectDao: ProjectDao?=null
    var songProject = CurrentProject()
        private set
    fun initDataBase(context: Context) {
        projectDatabase = DatabaseProvider.getDatabase(context)
        projectDao = projectDatabase?.projectDao()
    }
    suspend fun saveProject()  {
        Log.d("savingData","$songProject")
        projectDao?.insert(songProject.toProjectEntity())
    }
    suspend fun getAllProjects(): List<CurrentProject>? = projectDao?.getAll()?.map {
        it.toCurrentProject()
    }
}