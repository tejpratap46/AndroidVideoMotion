package com.tejpratapsingh.animator.worker

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tejpratapsingh.animator.notification.NotificationFactory
import com.tejpratapsingh.animator.presentation.sampleMotionVideo
import com.tejpratapsingh.motionlib.core.MotionVideoProducer
import com.tejpratapsingh.motionlib.worker.MotionWorker
import java.io.File
import java.net.URLConnection
import java.util.Locale
import java.util.UUID

class SampleMotionWorker(private val appContext: Context, parameters: WorkerParameters) :
    MotionWorker(appContext, parameters) {

    private val notificationManager = NotificationManagerCompat.from(appContext)

    private val progressNotificationBuilder: NotificationCompat.Builder by lazy {
        NotificationFactory.getRenderProgressNotification(appContext)
    }

    private val completedNotificationBuilder: NotificationCompat.Builder by lazy {
        NotificationFactory.getRenderCompleteNotification(appContext)
    }

    private fun createForegroundInfo(progressNotificationId: Int, notification: Notification): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ForegroundInfo(progressNotificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING)
        } else {
            ForegroundInfo(progressNotificationId, notification)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        // Create the notification for the foreground service
        val notification = progressNotificationBuilder
            .setContentTitle("Rendering Video...") // Initial title
            .setProgress(0, 0, true) // Indeterminate progress initially
            .setOngoing(true)
            .build()
        return createForegroundInfo(progressNotificationId, notification)
    }

    override fun getMotionVideo(inputData: Data): MotionVideoProducer {
        return sampleMotionVideo(appContext)
    }

    override fun onProgress(totalFrames: Int, currentProgress: Int, bitmap: Bitmap) {
        Log.d(TAG, "onProgress: $currentProgress / $totalFrames")

        val percentage = (currentProgress.toDouble() / totalFrames) * 100
        val progressText = String.format(
            Locale.getDefault(),
            "%d/%d frames completed",
            currentProgress,
            totalFrames
        )
        val contentText = String.format(Locale.getDefault(), "%.0f%%", percentage)

        val notification = progressNotificationBuilder
            .setProgress(totalFrames, currentProgress, false)
            .setSubText(progressText)
            .setContentText(contentText)
            .build()

        updateNotification(progressNotificationId, notification)

        // If you need to update the foreground notification specifically (often handled by the initial setForegroundAsync)
         setForegroundAsync(createForegroundInfo(progressNotificationId, notification))
    }

    override fun onCompleted(videoFile: File) {
        Log.d(TAG, "onCompleted: Video saved to ${videoFile.absolutePath}")

        // Cancel the progress notification
        notificationManager.cancel(progressNotificationId)

        val intentShareFile = Intent(Intent.ACTION_SEND)
        val apkURI: Uri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            videoFile
        )
        intentShareFile.setDataAndType(
            apkURI,
            URLConnection.guessContentTypeFromName(videoFile.name)
        )
        intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intentShareFile.putExtra(Intent.EXTRA_STREAM, apkURI)

        val pendingShareIntentFlags =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val pendingShareIntent = PendingIntent.getActivity(
            appContext,
            0, // requestCode, consider making this unique if you have many such intents
            intentShareFile,
            pendingShareIntentFlags
        )

        val completedNotification = completedNotificationBuilder
            .setContentTitle("Render Complete")
            .setContentText("Video ready: ${videoFile.name}")
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_menu_share, // Consider using a custom icon
                    "Share Video",
                    pendingShareIntent
                )
            )
            .setAutoCancel(true) // Dismiss notification when tapped (if no content intent set)
            .build()

        updateNotification(completedNotificationId, completedNotification)
    }

    private fun updateNotification(notificationId: Int, notification: Notification) {
        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(notificationId, notification)
        } else {
            // Handle the case where permission is not granted.
            // Maybe log an error or inform the user in a different way.
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot show notification.")
        }
    }

    companion object {
        private const val TAG = "SampleMotionWorker"

        fun startWork(context: Context): UUID {
            val workRequest = OneTimeWorkRequest.from(SampleMotionWorker::class.java)
            WorkManager.getInstance(context).enqueue(workRequest)
            return workRequest.id
        }
    }
}
