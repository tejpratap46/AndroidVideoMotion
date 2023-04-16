package com.tejpratapsingh.animator.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tejpratapsingh.animator.BuildConfig
import com.tejpratapsingh.animator.notification.NotificationFactory
import com.tejpratapsingh.animator.ui.view.ContourDevice
import com.tejpratapsingh.motionlib.core.MotionVideo
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.Orientation
import com.tejpratapsingh.motionlib.templates.views.DeviceFrameView
import com.tejpratapsingh.motionlib.ui.custom.background.GradientView
import com.tejpratapsingh.motionlib.utils.MotionConfig
import com.tejpratapsingh.motionlib.worker.MotionWorker
import java.io.File
import java.io.FileInputStream
import java.net.URLConnection
import java.util.*


class SampleMotionWorker(context: Context, parameters: WorkerParameters) :
    MotionWorker(context, parameters) {

    private val TAG = "SampleMotionWorker"

    private val progressNotificationBuilder: NotificationCompat.Builder by lazy {
        NotificationFactory.getRenderProgressNotification(context)
    }

    private val completedNotificationBuilder: NotificationCompat.Builder by lazy {
        NotificationFactory.getRenderCompleteNotification(context)
    }

    private val sampleMotionVideo: MotionVideo by lazy {
        val motionConfig = MotionConfig(
            width = 768,
            height = 1366,
            fps = 30
        )

        val motionView: MotionView = ContourDevice(
            context = applicationContext,
            startFrame = 1,
            endFrame = motionConfig.fps * 3
        )

        val motionView2: MotionView = GradientView(
            context = applicationContext,
            startFrame = motionConfig.fps * 3 + 1,
            endFrame = motionConfig.fps * 4,
            orientation = Orientation.CIRCULAR,
            intArrayOf(
                Color.parseColor("#2568ff"),
                Color.parseColor("#7048ff"),
                Color.parseColor("#ba28ff")
            )
        ).apply {
            setBackgroundColor(Color.WHITE)
        }

        val motionView3: MotionView = object : MotionView(context, motionConfig.fps * 4 + 1, motionConfig.fps * 6) {
            val imageFile = File.createTempFile("testImage", "jpg")

            val deviceFrameView: DeviceFrameView = DeviceFrameView(context = context, bitmap = BitmapFactory.decodeStream(FileInputStream(imageFile)))

            override fun forFrame(frame: Int): View {
                super.forFrame(frame)

                deviceFrameView.layoutBy(
                    x = leftTo {
                        parent.left()
                    }.rightTo {
                        parent.right()
                    },
                    y = topTo {
                        parent.top()
                    }.bottomTo {
                        parent.bottom()
                    }
                )

                contourHeightOf {
                    motionConfig.height.toYInt()
                }
                contourWidthOf {
                    motionConfig.width.toXInt()
                }

                return this
            }
        }

        MotionVideo.with(applicationContext, motionConfig)
            .addMotionViewToSequence(motionView)
            .addMotionViewToSequence(motionView2)
            .addMotionViewToSequence(motionView3)
    }

    override fun getMotionVideo(): MotionVideo {
        return sampleMotionVideo
    }

    override fun onProgress(totalFrames: Int, progress: Int, bitmap: Bitmap) {
        Log.d(TAG, "onProgress: $progress / $totalFrames")

        progressNotificationBuilder.setProgress(totalFrames, progress, false)
        progressNotificationBuilder.setSubText(
            String.format(
                Locale.getDefault(),
                "%d/%d frames completed",
                progress,
                totalFrames
            )
        )
        val percentage = (progress / totalFrames.toDouble()) * 100
        progressNotificationBuilder.setContentText(String.format(Locale.getDefault(), "%.0f", percentage))

        with(NotificationManagerCompat.from(applicationContext)) {
            // notificationId is a unique int for each notification that you must define
            notify(WORK_ID, progressNotificationBuilder.build())
        }
    }

    override fun onCompleted(videoFile: File) {
        Log.d(TAG, "onCompleted: ")

        with(NotificationManagerCompat.from(applicationContext)) {
            // notificationId is a unique int for each notification that you must define
            cancel(WORK_ID)
        }

        val intentShareFile = Intent(Intent.ACTION_SEND)

        val apkURI: Uri = FileProvider.getUriForFile(
            applicationContext,
            "${BuildConfig.APPLICATION_ID}.provider",
            videoFile
        )
        intentShareFile.setDataAndType(
            apkURI,
            URLConnection.guessContentTypeFromName(videoFile.name)
        )
        intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        intentShareFile.putExtra(
            Intent.EXTRA_STREAM,
            apkURI
        )

        val pendingShareIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intentShareFile,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        completedNotificationBuilder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_menu_share,
                "Share",
                pendingShareIntent
            )
        )

        with(NotificationManagerCompat.from(applicationContext)) {
            // notificationId is a unique int for each notification that you must define
            notify(WORK_ID, completedNotificationBuilder.build())
        }
    }

    companion object {
        fun startWork(context: Context) {
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    WORK_ID.toString(),
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequest.from(SampleMotionWorker::class.java)
                )
        }
    }
}