package com.tejpratapsingh.lyricsmaker.presentation.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tejpratapsingh.lyricsmaker.asLyricsApp
import com.tejpratapsingh.motionlib.core.extensions.md5
import com.tejpratapsingh.motionstore.tables.MotionProject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * Instrumented tests for [LyricsMotionWorker], covering the refactored
 * [LyricsMotionWorker.startWork] API that now accepts a [projectId] instead of
 * the old song/lyrics/image triple.
 */
@RunWith(AndroidJUnit4::class)
class LyricsMotionWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val songName = "Worker Test Song"
    private val projectId = songName.md5()

    @Before
    fun setUp() {
        // Insert a MotionProject so the worker can load it by id.
        val project =
            MotionProject(
                id = projectId,
                name = songName,
                path = "/$projectId",
                metadata =
                    JsonObject().apply {
                        addProperty("image", "https://example.com/img.png")
                        addProperty("startTime", 0f)
                        add(
                            "lyrics",
                            JsonArray().apply {
                                add(
                                    JsonObject().apply {
                                        addProperty("frame", 0)
                                        addProperty("text", "Hello")
                                    },
                                )
                                add(
                                    JsonObject().apply {
                                        addProperty("frame", 48)
                                        addProperty("text", "World")
                                    },
                                )
                            },
                        )
                    },
            )
        context.asLyricsApp().motionStoreDao.upsert(project)
    }

    // ------------------------------------------------------------------
    // startWork(context, projectId) – new API introduced in this PR
    // ------------------------------------------------------------------

    @Test
    fun startWork_returnsNonNullUUID() {
        val workId: UUID = LyricsMotionWorker.startWork(context, projectId)

        assertNotNull("startWork should return a valid UUID", workId)

        // Clean up – cancel immediately so the worker doesn't actually run.
        WorkManager.getInstance(context).cancelWorkById(workId)
    }

    @Test
    fun startWork_enqueuedWorkIdMatchesReturnedUUID() {
        val workId: UUID = LyricsMotionWorker.startWork(context, projectId)

        // WorkManager should be able to look up a work info for the returned UUID.
        val workInfoFuture = WorkManager.getInstance(context).getWorkInfoById(workId)
        val workInfo: WorkInfo? = workInfoFuture.get()

        assertNotNull("WorkManager should find a WorkInfo for the returned UUID", workInfo)
        assertEquals(workId, workInfo!!.id)

        // Clean up.
        WorkManager.getInstance(context).cancelWorkById(workId)
    }

    @Test
    fun startWork_enqueuedWorkIsInQueuedOrRunningState() {
        val workId: UUID = LyricsMotionWorker.startWork(context, projectId)

        val workInfo: WorkInfo? = WorkManager.getInstance(context).getWorkInfoById(workId).get()
        assertNotNull(workInfo)

        val state = workInfo!!.state
        // The work should be either ENQUEUED or RUNNING immediately after startWork().
        val isActive = state == WorkInfo.State.ENQUEUED || state == WorkInfo.State.RUNNING
        assertEquals(
            "Newly started work should be ENQUEUED or RUNNING, but was $state",
            true,
            isActive,
        )

        // Clean up.
        WorkManager.getInstance(context).cancelWorkById(workId)
    }

    @Test
    fun startWork_differentProjectIdsProduceDifferentWorkIds() {
        val id1 = "project-alpha".md5()
        val id2 = "project-beta".md5()

        // Insert minimal projects for both ids so startWork can be called.
        listOf(id1 to "Project Alpha", id2 to "Project Beta").forEach { (pid, name) ->
            context.asLyricsApp().motionStoreDao.upsert(
                MotionProject(
                    id = pid,
                    name = name,
                    path = "/$pid",
                    metadata =
                        JsonObject().apply {
                            add("lyrics", JsonArray())
                        },
                ),
            )
        }

        val workId1 = LyricsMotionWorker.startWork(context, id1)
        val workId2 = LyricsMotionWorker.startWork(context, id2)

        // Each call should produce its own unique WorkRequest UUID.
        assertNotNull(workId1)
        assertNotNull(workId2)
        assertEquals(
            "Two separate startWork calls should return different UUIDs",
            false,
            workId1 == workId2,
        )

        WorkManager.getInstance(context).cancelWorkById(workId1)
        WorkManager.getInstance(context).cancelWorkById(workId2)
    }
}