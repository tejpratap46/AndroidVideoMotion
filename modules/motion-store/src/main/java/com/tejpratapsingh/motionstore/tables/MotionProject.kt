package com.tejpratapsingh.motionstore.tables

import com.google.gson.JsonObject
import com.tejpratapsingh.motionstore.domain.SyncableEntity
import com.tejpratapsingh.motionstore.infra.DeviceInfo
import java.util.UUID

data class MotionProject(
    override val id: String,
    val name: String,
    val path: String,
    val sdui: JsonObject = JsonObject(),
    val metadata: JsonObject = JsonObject(),
    val created: Long = System.currentTimeMillis(),
    val updated: Long = System.currentTimeMillis(),
    override val syncTracker: SyncTracker = SyncTracker(updatedBy = DeviceInfo.id),
) : SyncableEntity {
    override fun withSyncTracker(tracker: SyncTracker) = copy(syncTracker = tracker)
}

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
    ).also {
        ProjectStore.motionProject = it
    }
