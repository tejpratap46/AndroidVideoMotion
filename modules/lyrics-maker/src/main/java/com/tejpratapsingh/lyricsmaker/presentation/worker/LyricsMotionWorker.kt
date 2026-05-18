package com.tejpratapsingh.lyricsmaker.presentation.worker

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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tejpratapsingh.lyricsmaker.R
import com.tejpratapsingh.lyricsmaker.asLyricsApp
import com.tejpratapsingh.lyricsmaker.presentation.notification.NotificationFactory
import com.tejpratapsingh.motion.sdui.infra.SDUIMotionVideoProducerFactory
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.ffmpeg.FfmpegVideoProducerAdapter
import com.tejpratapsingh.motionlib.worker.MotionWorker
import com.tejpratapsingh.motionstore.extensions.createProjectFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.URLConnection
import java.util.Locale
import java.util.UUID

class LyricsMotionWorker(
    private val appContext: Context,
    parameters: WorkerParameters,
) : MotionWorker(appContext, parameters) {
    private val notificationManager = NotificationManagerCompat.from(appContext)

    private val progressNotificationBuilder: NotificationCompat.Builder by lazy {
        NotificationFactory.getRenderProgressNotification(appContext)
    }

    private val completedNotificationBuilder: NotificationCompat.Builder by lazy {
        NotificationFactory.getRenderCompleteNotification(appContext)
    }

    private fun createForegroundInfo(
        progressNotificationId: Int,
        notification: Notification,
    ): ForegroundInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ForegroundInfo(
                progressNotificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING,
            )
        } else {
            ForegroundInfo(progressNotificationId, notification)
        }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        // Create the notification for the foreground service
        val notification =
            progressNotificationBuilder
                .setContentTitle("Rendering Video...") // Initial title
                .setProgress(0, 0, true) // Indeterminate progress initially
                .setOngoing(true)
                .clearActions()
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    appContext.getString(R.string.cancel),
                    createCancelPendingIntent(),
                ).build()
        return createForegroundInfo(progressNotificationId, notification)
    }

    override suspend fun getOutputFile(): File =
        withContext(Dispatchers.Unconfined) {
            val projectId = inputData.getString(PROJECT_ID)!!
            val motionProject = applicationContext.asLyricsApp().motionStoreDao.findById(projectId)!!
            applicationContext.createProjectFile(motionProject)
        }

    override fun getMotionVideo(inputData: Data): MotionVideoProducer {
        val projectId = inputData.getString(PROJECT_ID)!!
        val motionProject = applicationContext.asLyricsApp().motionStoreDao.findById(projectId)!!
        return SDUIMotionVideoProducerFactory(
            context = appContext,
            videoProducerAdapter = FfmpegVideoProducerAdapter(),
        ).createFromProject(motionProject)
    }

    override suspend fun onProgress(
        totalFrames: Int,
        currentProgress: Int,
        bitmap: Bitmap,
    ) {
        Timber.d("onProgress: $currentProgress / $totalFrames")

        val percentage = (currentProgress.toDouble() / totalFrames) * 100
        val progressText =
            String.format(
                Locale.getDefault(),
                "%d/%d frames completed",
                currentProgress,
                totalFrames,
            )
        val contentText = String.format(Locale.getDefault(), "%.0f%%", percentage)

        val notification =
            progressNotificationBuilder
                .setProgress(totalFrames, currentProgress, false)
                .setSubText(progressText)
                .setContentText(contentText)
                .setOngoing(true)
                .clearActions()
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    appContext.getString(R.string.cancel),
                    createCancelPendingIntent(),
                ).build()

        updateNotification(progressNotificationId, notification)

        // If you need to update the foreground notification specifically (often handled by the initial setForegroundAsync)
        setForegroundAsync(createForegroundInfo(progressNotificationId, notification))
    }

    override suspend fun onCompleted(videoFile: File) {
        Timber.d("onCompleted: Video saved to ${videoFile.absolutePath}")

        val projectId = inputData.getString(PROJECT_ID)!!
        val motionProject = applicationContext.asLyricsApp().motionStoreDao.findById(projectId)!!
        Timber.i("onCompleted: $motionProject")
        applicationContext.asLyricsApp().motionStoreDao.upsert(motionProject)

        // Cancel the progress notification
        notificationManager.cancel(progressNotificationId)

        val intentShareFile = Intent(Intent.ACTION_SEND)
        val pendingShareIntent = createPendingIntentFor(intentShareFile, videoFile)
        val intentOpenFile = Intent(Intent.ACTION_VIEW)
        val pendingOpenFileIntent = createPendingIntentFor(intentOpenFile, videoFile)

        val completedNotification =
            completedNotificationBuilder
                .setContentTitle("Render Complete")
                .setContentText("Video ready: ${videoFile.name}")
                .setOngoing(false)
                .clearActions()
                .addAction(
                    android.R.drawable.ic_menu_share, // Consider using a custom icon
                    "Share Video",
                    pendingShareIntent,
                ).addAction(
                    android.R.drawable.ic_media_play, // Consider using a custom icon
                    "Open Video",
                    pendingOpenFileIntent,
                ).build()

        updateNotification(completedNotificationId, completedNotification)
    }

    @Volatile
    private var lastNotificationUpdateTime = 0L

    private fun updateNotification(
        notificationId: Int,
        notification: Notification,
    ) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNotificationUpdateTime < 1000) {
            return
        }
        lastNotificationUpdateTime = currentTime

        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(notificationId, notification)
        } else {
            // Handle the case where permission is not granted.
            // Maybe log an error or inform the user in a different way.
            Timber.w("POST_NOTIFICATIONS permission not granted. Cannot show notification.")
        }
    }

    private fun createPendingIntentFor(
        intent: Intent,
        videoFile: File,
    ): PendingIntent {
        val videoFileUri: Uri =
            FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                videoFile,
            )
        intent.setDataAndType(
            videoFileUri,
            URLConnection.guessContentTypeFromName(videoFile.name),
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_STREAM, videoFileUri)

        val pendingShareIntentFlags =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return PendingIntent.getActivity(
            appContext,
            0, // requestCode, consider making this unique if you have many such intents
            intent,
            pendingShareIntentFlags,
        )
    }

    private fun createCancelPendingIntent(): PendingIntent {
        val intent =
            Intent(appContext, LyricsMotionWorkerCancelReceiver::class.java).apply {
                action = LyricsMotionWorkerCancelReceiver.ACTION_CANCEL
                putExtra(LyricsMotionWorkerCancelReceiver.EXTRA_WORK_ID, id.toString())
            }
        return PendingIntent.getBroadcast(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val PROJECT_ID = "project_id"

        fun startWork(
            context: Context,
            projectId: String,
        ): UUID {
            val inputData =
                Data
                    .Builder()
                    .putString(PROJECT_ID, projectId)
                    .build()

            val workRequest =
                OneTimeWorkRequestBuilder<LyricsMotionWorker>()
                    .addTag(getWorkTag(projectId))
                    .setInputData(inputData)
                    .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            return workRequest.id
        }

        fun getWorkTag(projectId: String): String = "project_$projectId"

        fun cancelAllWork(context: Context) {
            WorkManager.getInstance(context).cancelAllWork()
        }
    }
}
