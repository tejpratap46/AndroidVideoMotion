package com.tejpratapsingh.animator.ui.view

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.animator.R
import com.tejpratapsingh.motionlib.core.MotionVideo
import com.tejpratapsingh.motionlib.ui.MotionVideoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class MotionVideoContainer(context: Context, motionVideo: MotionVideo) :
    ContourLayout(context) {

    private val TAG = "MotionVideoContainer"

    private val toolbar: Toolbar = Toolbar(context).apply {
        title = "Video"
        subtitle = "Create New"
        setBackgroundColor(Color.CYAN)
    }

    private val videoPlayer: MotionVideoPlayer = MotionVideoPlayer(
        context, motionVideo
    )

    private val exportVideo: Button = Button(context).apply {
        text = context.getString(R.string.export_video)

        val scope = CoroutineScope(
            Dispatchers.Main + SupervisorJob()
        )

        setOnClickListener {
            visibility = GONE

            scope.launch {
                val uri: Uri = generateVideo(
                    motionVideo = motionVideo,
                    progressListener = { progress, bitmap ->
                        scope.launch {
                            Log.d(TAG, "Progress: $progress")
                            videoPlayer.imagePreview.setImageBitmap(bitmap)
                            videoPlayer.seekBar.progress = progress
                        }
                    }
                )

                visibility = VISIBLE
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "video/*"
                    putExtra(Intent.EXTRA_STREAM, uri)

                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share File"))
            }
        }
    }

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

        exportVideo.layoutBy(
            x = leftTo {
                parent.left()
            }.rightTo {
                parent.right()
            },
            y = bottomTo {
                parent.bottom()
            }
        )

        videoPlayer.layoutBy(
            x = leftTo {
                parent.left()
            }.rightTo {
                parent.right()
            },
            y = topTo {
                toolbar.bottom()
            }.bottomTo {
                exportVideo.top()
            }
        )
    }

    suspend fun generateVideo(
        motionVideo: MotionVideo,
        progressListener: ((progress: Int, bitmap: Bitmap) -> Unit)?
    ): Uri =
        withContext(Dispatchers.IO) {
            val fileToShare = motionVideo.produceVideo(
                outputFile = File.createTempFile(
                    "out",
                    ".mp4",
                    context.filesDir
                ),
                progressListener = progressListener
            )

            FileProvider.getUriForFile(
                context,
                context.getString(R.string.authority),
                fileToShare
            )
        }
}