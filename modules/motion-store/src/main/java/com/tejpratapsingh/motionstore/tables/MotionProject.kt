package com.tejpratapsingh.motionstore.tables

import java.util.UUID

data class MotionProject(
    val id: String,
    val name: String,
    val path: String,
    val sdui: String? = null,
    val metadata: String? = null,
    val created: Long = System.currentTimeMillis(),
    val updated: Long = System.currentTimeMillis(),
)

private object ProjectStore {
    @Volatile
    var motionProject: MotionProject? = null
}

fun setCurrentProject(motionProject: MotionProject) {
    ProjectStore.motionProject = motionProject
}

fun provideCurrentProject(id: String = UUID.randomUUID().toString()): MotionProject =
    ProjectStore.motionProject ?: MotionProject(
        id = id,
        name = "",
        path = "/$id",
        sdui = null,
    ).also {
        ProjectStore.motionProject = it
    }
