package com.tejpratapsingh.motion.download

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.ketch.Ketch
import com.ketch.Status
import com.tejpratapsingh.motion.sdui.infra.MotionAssetExtractor
import com.tejpratapsingh.motionstore.dao.MotionProjectDao
import com.tejpratapsingh.motionstore.infra.DatabaseManager
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.io.File
import java.util.UUID

class MotionDownloadWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    private val kv by lazy {
        MMKV.initialize(applicationContext)
        MMKV.mmkvWithID("motion_download_cache")
    }
    private val ketch = Ketch.builder().build(applicationContext)
    private val downloadDir =
        File(applicationContext.cacheDir, "downloads").apply {
            if (!exists() && !mkdirs()) {
                throw IllegalStateException("Failed to create download directory: $absolutePath")
            }
        }

    private val projectDao by lazy {
        MotionProjectDao(DatabaseManager.getInstance())
    }

    override suspend fun doWork(): Result {
        val projectId = inputData.getString(KEY_PROJECT_ID) ?: return Result.failure()
        Timber.d("Starting work for project: $projectId")
        val project =
            projectDao.findById(projectId) ?: run {
                Timber.e("Project not found: $projectId")
                return Result.failure()
            }

        val assets =
            MotionAssetExtractor
                .extractAssets(applicationContext, project.sdui)
                .filter { asset ->
                    val uri = asset.getUri()
                    uri.scheme == "http" || uri.scheme == "https"
                }

        Timber.d("Extracted ${assets.size} remote assets for project: $projectId")

        if (assets.isEmpty()) {
            Timber.w("Assets are empty, Exiting as success")
            return Result.success()
        }

        val downloadIds = mutableListOf<Int>()

        assets.forEach { asset ->
            val url = asset.getUri().toString()
            val deterministicName = UUID.nameUUIDFromBytes(url.toByteArray()).toString()
            val extension =
                MimeTypeMap.getFileExtensionFromUrl(url).let {
                    if (it.isNotEmpty()) ".$it" else ""
                }
            val finalFileName = deterministicName + extension
            val cachedPath = kv.decodeString(url)

            if (cachedPath == null || !File(cachedPath).exists() || !File(cachedPath).isFile) {
                Timber.d("Starting download for: $url")
                val id =
                    ketch.download(
                        url = url,
                        path = downloadDir.absolutePath,
                        fileName = finalFileName,
                    )
                downloadIds.add(id)
            } else {
                Timber.d("Asset already cached: $url -> $cachedPath")
            }
        }

        if (downloadIds.isEmpty()) {
            Timber.d("No new downloads needed for project: $projectId")
            return Result.success()
        }

        Timber.d("Waiting for ${downloadIds.size} downloads to finish")

        // Wait for all downloads to finish
        return try {
            ketch.observeDownloads().first { downloads ->
                val relevantDownloads = downloads.filter { it.id in downloadIds }

                // Update MMKV for successful downloads
                relevantDownloads.forEach { model ->
                    if (model.status == Status.SUCCESS) {
                        val fullPath = File(model.path, model.fileName).absolutePath
                        Timber.d("Download successful, updating cache: ${model.url} -> $fullPath")
                        kv.encode(model.url, fullPath)
                    } else if (model.status == Status.FAILED) {
                        Timber.e("Download failed: ${model.url}, reason: ${model.failureReason}")
                    }
                }

                val allFinished = relevantDownloads.all { it.status == Status.SUCCESS || it.status == Status.FAILED }
                if (allFinished) {
                    Timber.d("All downloads finished for project: $projectId")
                }
                allFinished && relevantDownloads.isNotEmpty()
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error waiting for downloads")
            Result.retry()
        }
    }

    companion object {
        const val KEY_PROJECT_ID = "key_project_id"
    }
}
