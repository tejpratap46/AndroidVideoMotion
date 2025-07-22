package com.tejpratapsingh.animator.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tejpratapsingh.animator.BuildConfig
import com.tejpratapsingh.animator.notification.NotificationFactory
import com.tejpratapsingh.animator.presentation.sampleMotionVideo
import com.tejpratapsingh.motionlib.core.MotionVideo
import com.tejpratapsingh.motionlib.worker.MotionWorker
import java.io.File
import java.net.URLConnection
import java.util.Locale


class SampleMotionWorker(context: Context, parameters: WorkerParameters) :
    MotionWorker(context, parameters) {

    private val TAG = "SampleMotionWorker"

    private val progressNotificationBuilder: NotificationCompat.Builder by lazy {
        NotificationFactory.getRenderProgressNotification(applicationContext)
    }

    private val completedNotificationBuilder: NotificationCompat.Builder by lazy {
        NotificationFactory.getRenderCompleteNotification(applicationContext)
    }

    override fun getMotionVideo(inputData: Data): MotionVideo {
        return sampleMotionVideo(applicationContext)
    }

    override fun onProgress(totalFrames: Int, currentProgress: Int, bitmap: Bitmap) {
        Log.d(TAG, "onProgress: $currentProgress / $totalFrames")

        progressNotificationBuilder.setProgress(totalFrames, currentProgress, false)
        progressNotificationBuilder.setSubText(
            String.format(
                Locale.getDefault(),
                "%d/%d frames completed",
                currentProgress,
                totalFrames
            )
        )
        val percentage = (currentProgress / totalFrames.toDouble()) * 100
        progressNotificationBuilder.setContentText(
            String.format(
                Locale.getDefault(),
                "%.0f",
                percentage
            )
        )

        with(NotificationManagerCompat.from(applicationContext)) {
            // notificationId is a unique int for each notification that you must define
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(
                    this@SampleMotionWorker.id.clockSequence(),
                    progressNotificationBuilder.build()
                )
            }
        }
    }

    override fun onCompleted(videoFile: File) {
        Log.d(TAG, "onCompleted: ")

        with(NotificationManagerCompat.from(applicationContext)) {
            // notificationId is a unique int for each notification that you must define
            cancel(this@SampleMotionWorker.id.clockSequence())
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
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(
                    this@SampleMotionWorker.id.clockSequence(),
                    completedNotificationBuilder.build()
                )
            }
        }
    }

    companion object {
        fun startWork(context: Context) {
            WorkManager
                .getInstance(context)
                .enqueue(
                    OneTimeWorkRequest.from(SampleMotionWorker::class.java)
                )
        }
    }
}