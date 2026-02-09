package com.tejpratapsingh.motionstore.tables

import com.tejpratapsingh.motionstore.infra.AppDatabaseHelper
import com.tejpratapsingh.motionstore.infra.provideMotionProjectDao
import java.util.UUID

data class MotionProject(
    val id: String,
    val name: String,
    val path: String,
    val sdui: String? = null,
    val created: Long = System.currentTimeMillis(),
    val updated: Long = System.currentTimeMillis(),
)

fun AppDatabaseHelper.createOrSaveProject(project: MotionProject) {
    val oldMotionProject = provideMotionProjectDao(this).getById(project.id)
    if (oldMotionProject != null) {
        provideMotionProjectDao(this).update(project)
    } else {
        provideMotionProjectDao(this).insert(project)
    }
}

fun AppDatabaseHelper.getAllProjects(): List<MotionProject> = provideMotionProjectDao(this).getAll()

private object ProjectStore {
    @Volatile
    var motionProject: MotionProject? = null
}

fun setCurrentProject(motionProject: MotionProject) {
    ProjectStore.motionProject = motionProject
}

fun provideCurrentProject(): MotionProject {
    val id = UUID.randomUUID().toString()
    return ProjectStore.motionProject ?: MotionProject(
        id = id,
        name = "",
        path = "/$id",
        sdui = null,
    ).also {
        ProjectStore.motionProject = it
    }
}
