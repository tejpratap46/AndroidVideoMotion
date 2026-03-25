package com.tejpratapsingh.motionstore.worker

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.tejpratapsingh.motionstore.dao.SyncableDao
import com.tejpratapsingh.motionstore.domain.SyncException
import com.tejpratapsingh.motionstore.infra.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * A [CoroutineWorker] that drives a full [SyncManager] sync cycle.
 *
 * Designed to be scheduled via [WorkManager] for both periodic background
 * sync and on-demand sync (e.g. after a local write or on network reconnect).
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val syncManager: SyncManager,
    private val daos: List<SyncableDao<*>>,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting sync work (attempt $runAttemptCount)")
                val results = syncManager.sync(daos)
                Log.d(TAG, "Sync completed successfully. Results: ${results.size} tables synced")

                val anyNetworkFailure =
                    results.any { result ->
                        result.error is SyncException.NetworkError
                    }

                if (anyNetworkFailure && runAttemptCount < MAX_ATTEMPTS) {
                    Log.w(
                        TAG,
                        "Network error detected in results. Retrying (attempt ${runAttemptCount + 1}/$MAX_ATTEMPTS)",
                    )
                    return@withContext Result.retry()
                }

                val output =
                    workDataOf(
                        KEY_DOWNLOADED to results.sumOf { it.downloaded },
                        KEY_UPLOADED to results.sumOf { it.uploaded },
                        KEY_CONFLICTS to results.sumOf { it.conflicts },
                        KEY_UPLOAD_FAILS to results.sumOf { it.uploadFailed },
                        KEY_TABLES to results.joinToString(",") { it.tableName },
                    )

                Result.success(output)
            } catch (e: SyncException.NetworkError) {
                Log.w(TAG, "Network error caught: ${e.message}", e)
                if (runAttemptCount < MAX_ATTEMPTS) {
                    Result.retry()
                } else {
                    Result.failure(workDataOf(KEY_ERROR to e.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during sync: ${e.message}", e)
                Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Unknown error")))
            }
        }

    companion object {
        private const val TAG = "SyncWorker"

        const val TAG_PERIODIC = "sync_periodic"
        const val TAG_IMMEDIATE = "sync_immediate"

        const val KEY_DOWNLOADED = "downloaded"
        const val KEY_UPLOADED = "uploaded"
        const val KEY_CONFLICTS = "conflicts"
        const val KEY_UPLOAD_FAILS = "upload_fails"
        const val KEY_TABLES = "tables"
        const val KEY_ERROR = "error"

        private const val MAX_ATTEMPTS = 3
        private const val MIN_BACKOFF_SECONDS = 30L

        /**
         * Enforce network connectivity for sync tasks.
         * This prevents UnknownHostException by ensuring the worker only runs when
         * the device thinks it has a valid connection.
         */
        val defaultConstraints: Constraints =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        fun schedulePeriodic(
            context: Context,
            intervalMinutes: Long = 30,
            constraints: Constraints = defaultConstraints,
        ) {
            val request =
                PeriodicWorkRequestBuilder<SyncWorker>(
                    repeatInterval = intervalMinutes,
                    repeatIntervalTimeUnit = TimeUnit.MINUTES,
                ).setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        MIN_BACKOFF_SECONDS,
                        TimeUnit.SECONDS,
                    ).addTag(TAG_PERIODIC)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                TAG_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun scheduleImmediate(
            context: Context,
            constraints: Constraints = defaultConstraints,
        ) {
            val request =
                OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        MIN_BACKOFF_SECONDS,
                        TimeUnit.SECONDS,
                    ).addTag(TAG_IMMEDIATE)
                    .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                TAG_IMMEDIATE,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        fun cancelAll(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(TAG_PERIODIC)
            WorkManager.getInstance(context).cancelAllWorkByTag(TAG_IMMEDIATE)
        }
    }
}

class SyncWorkerFactory(
    private val syncManager: SyncManager,
    private val daos: List<SyncableDao<*>>,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return when (workerClassName) {
            SyncWorker::class.java.name -> {
                SyncWorker(appContext, workerParameters, syncManager, daos)
            }
            else -> null
        }
    }
}
