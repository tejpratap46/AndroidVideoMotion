package com.tejpratapsingh.motion.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.tejpratapsingh.motionstore.tables.MotionProject

class MotionDownloadWorker(
    context: Context,
    params: WorkerParameters,
    private val controller: MotionDownloadController,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        controller.downloadProjectAssets(
            projectId = inputData.getString(KEY_PROJECT_ID) ?: return Result.failure(),
        )

    companion object {
        const val KEY_PROJECT_ID = "key_project_id"

        fun enqueueWork(
            context: Context,
            project: MotionProject,
        ) {
            val workRequest =
                OneTimeWorkRequestBuilder<MotionDownloadWorker>()
                    .setInputData(workDataOf(KEY_PROJECT_ID to project.id))
                    .setConstraints(
                        Constraints
                            .Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build(),
                    ).build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "motion_download_${project.id}",
                ExistingWorkPolicy.KEEP,
                workRequest,
            )
        }
    }
}
