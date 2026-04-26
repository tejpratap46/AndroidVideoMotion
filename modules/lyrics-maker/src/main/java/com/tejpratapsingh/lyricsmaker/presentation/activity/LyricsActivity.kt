package com.tejpratapsingh.lyricsmaker.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tejpratapsingh.lyricsmaker.asLyricsApp
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.motion.getLyricsVideoProducer
import com.tejpratapsingh.lyricsmaker.presentation.motion.getMultiLyricsVideoProducer
import com.tejpratapsingh.lyricsmaker.presentation.worker.LyricsMotionWorker
import com.tejpratapsingh.motion.metadataextractor.data.SocialMeta
import com.tejpratapsingh.motion.metadataextractor.presentation.ShareReceiverActivity
import com.tejpratapsingh.motionlib.activities.PreviewActivity
import com.tejpratapsingh.motionlib.core.extensions.md5
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionstore.tables.MotionProject
import com.tejpratapsingh.motionstore.tables.provideCurrentProject
import com.tejpratapsingh.motionstore.tables.setCurrentProject

class LyricsActivity : PreviewActivity() {
    companion object {
        const val PROJECT_ID = "project_id"

        fun start(
            context: Context,
            projectId: String,
        ) {
            context.startActivity(
                Intent(context, LyricsActivity::class.java).also {
                    it.putExtra(PROJECT_ID, projectId)
                },
            )
        }
    }

    private val projectId: String?
        get() = intent.getStringExtra(PROJECT_ID)

    private val project: MotionProject? by lazy {
        projectId?.let { id -> applicationContext.asLyricsApp().motionStoreDao.findById(id) }
    }

    private val song: String
        get() = project?.name ?: ""

    private val lyrics: List<SyncedLyricFrame>
        get() {
            val metadata = project?.metadata
            val projectLyrics =
                metadata?.get("lyrics")?.takeIf { it.isJsonArray }?.asJsonArray?.map {
                    SyncedLyricFrame(
                        frame =
                            it.asJsonObject
                                .get("frame")
                                ?.takeIf { f -> f.isJsonPrimitive }
                                ?.asInt
                                ?: 0,
                        text =
                            it.asJsonObject
                                .get("text")
                                ?.takeIf { t -> t.isJsonPrimitive }
                                ?.asString
                                ?: "",
                    )
                }
            if (projectLyrics != null) return projectLyrics

            return emptyList()
        }

    private val video by lazy {
        val currentProject = project ?: provideCurrentProject(id = song.md5()).copy(name = song)
        setCurrentProject(currentProject)

        getMultiLyricsVideoProducer(
            applicationContext = applicationContext,
            motionProject = currentProject,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val start = if (lyrics.isNotEmpty()) lyrics.minBy { it.frame }.frame else 0
        val end = if (lyrics.isNotEmpty()) lyrics.maxBy { it.frame }.frame else 0

        MaterialAlertDialogBuilder(this)
            .setTitle("Lyrics")
            .setMessage(
                """
                Rendering video for \"$song\" with ${lyrics.size} lines of lyrics.
                Start Frame: $start
                End Frame: ${getMotionVideo().totalFrames}
                Duration: ${(end - start)} frames (${(end - start) / provideCurrentConfig().fps} seconds)
                """.trimIndent(),
            ).setPositiveButton("OK") { dialog, _ ->
                val currentProject = project ?: provideCurrentProject(id = song.md5()).copy(name = song)
                applicationContext.asLyricsApp().motionStoreDao.upsert(currentProject)

                LyricsMotionWorker.startWork(
                    context = applicationContext,
                    projectId = currentProject.id,
                )
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.setCancelable(false)
            .show()
    }

    override fun getMotionVideo(): MotionVideoProducer = video
}
