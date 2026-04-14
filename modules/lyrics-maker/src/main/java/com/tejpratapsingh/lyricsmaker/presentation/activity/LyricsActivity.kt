package com.tejpratapsingh.lyricsmaker.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.motion.getLyricsVideoProducer
import com.tejpratapsingh.lyricsmaker.presentation.worker.LyricsMotionWorker
import com.tejpratapsingh.motion.metadataextractor.data.SocialMeta
import com.tejpratapsingh.motion.metadataextractor.presentation.ShareReceiverActivity
import com.tejpratapsingh.motionlib.activities.PreviewActivity
import com.tejpratapsingh.motionlib.core.extensions.md5
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionstore.tables.provideCurrentProject
import com.tejpratapsingh.motionstore.tables.setCurrentProject

class LyricsActivity : PreviewActivity() {
    companion object {
        const val SONG = "song"
        const val LYRICS = "lyrics"

        fun start(
            context: Context,
            song: String,
            lyrics: ArrayList<SyncedLyricFrame>,
            socialMeta: SocialMeta? = null,
        ) {
            context.startActivity(
                Intent(context, LyricsActivity::class.java).also {
                    it.putExtra(SONG, song)
                    it.putExtra(ShareReceiverActivity.EXTRA_METADATA, socialMeta)
                    it.putParcelableArrayListExtra(LYRICS, lyrics)
                },
            )
        }
    }

    private val song: String
        get() = intent.getStringExtra(SONG) ?: ""

    private val lyrics: List<SyncedLyricFrame>
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra(LYRICS, SyncedLyricFrame::class.java)?.toList()
                    ?: emptyList()
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra(LYRICS) ?: emptyList()
            }

    private val socialMeta
        get() = ShareReceiverActivity.readMetadataFromIntent(intent)

    private val video by lazy {
        setCurrentProject(
            provideCurrentProject(id = song.md5()).copy(name = song),
        )
        getLyricsVideoProducer(
            applicationContext = applicationContext,
            song = song,
            lyrics = lyrics,
            image = socialMeta?.image,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val start = lyrics.minBy { it.frame }.frame
        val end = lyrics.maxBy { it.frame }.frame

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
                LyricsMotionWorker.startWork(
                    context = applicationContext,
                    song = song,
                    lyrics = lyrics,
                    image = socialMeta?.image,
                )
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.setCancelable(false)
            .show()
    }

    override fun getMotionVideo(): MotionVideoProducer = video
}
