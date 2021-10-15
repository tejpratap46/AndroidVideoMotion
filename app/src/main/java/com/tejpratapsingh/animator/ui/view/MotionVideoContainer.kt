package com.tejpratapsingh.animator.ui.view

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.animator.R
import com.tejpratapsingh.motionlib.ui.MotionComposerView
import kotlinx.coroutines.*
import java.io.File


class MotionVideoContainer(context: Context, motionComposerView: MotionComposerView) :
    ContourLayout(context) {

    val toolbar: Toolbar = Toolbar(context).apply {
        title = "Video"
        subtitle = "Create New"
        setBackgroundColor(Color.CYAN)
    }

    val seekBar: SeekBar = SeekBar(context).apply {
        max = motionComposerView.motionConfig.totalFrames

        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                motionComposerView.forFrame(p1 + 1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
    }

    val exportVideo: Button = Button(context).apply {
        text = "Export Video"

        val scope: CoroutineScope = CoroutineScope(
            Dispatchers.Main + SupervisorJob()
        )

        setOnClickListener {
            visibility = GONE

            scope.launch {
                val uri: Uri = generateVideo(motionComposerView = motionComposerView)

                visibility = VISIBLE
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "video/mp4"
                    putExtra(Intent.EXTRA_STREAM, uri)

                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share File"))
            }
        }
    }

    val controlsLayout: LinearLayoutCompat = LinearLayoutCompat(context).apply {
        orientation = LinearLayoutCompat.VERTICAL
        setBackgroundColor(Color.RED)
    }

    val previewLayout: LinearLayoutCompat = LinearLayoutCompat(context).apply {
        orientation = LinearLayoutCompat.VERTICAL
        setBackgroundColor(Color.GREEN)
    }

    val progressBar: ProgressBar = ProgressBar(context)

    init {
        toolbar.layoutBy(
            x = leftTo {
                parent.left()
            }.rightTo {
                parent.right()
            },
            y = topTo {
                parent.top()
            }
        )

        controlsLayout.addView(seekBar)
        controlsLayout.addView(exportVideo)

        controlsLayout.layoutBy(
            x = leftTo {
                parent.left()
            }.rightTo {
                parent.right()
            },
            y = bottomTo {
                parent.bottom()
            }
        )

//        previewLayout.addView(motionComposerView)
//        previewLayout.addView(progressBar)
        previewLayout.layoutBy(
            x = leftTo {
                parent.left()
            }.rightTo {
                parent.right()
            },
            y = topTo {
                toolbar.bottom()
            }.bottomTo {
                controlsLayout.top()
            }
        )
    }

    suspend fun generateVideo(motionComposerView: MotionComposerView): Uri =
        withContext(Dispatchers.IO) {
            val fileToShare = motionComposerView.produceVideo(
                File.createTempFile(
                    "out",
                    ".mp4",
                    context.filesDir
                )
            )

            FileProvider.getUriForFile(
                context,
                context.getString(R.string.authority),
                fileToShare
            )
        }
}