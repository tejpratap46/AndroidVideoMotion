package com.tejpratapsingh.motion.download

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.work.ListenableWorker
import com.ketch.Ketch
import com.ketch.Status
import com.tejpratapsingh.motion.sdui.infra.MotionAssetExtractor
import com.tejpratapsingh.motionlib.core.MotionAssetManager
import com.tejpratapsingh.motionstore.dao.MotionProjectDao
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.io.File
import java.util.UUID

class MotionDownloadController(
    private val context: Context,
    private val projectDao: MotionProjectDao,
    private val ketch: Ketch,
    private val kv: MMKV,
    private val assetManager: MotionAssetManager,
) {
    private val downloadDir =
        File(context.cacheDir, "downloads").apply {
            if (!exists() && !mkdirs()) {
                throw IllegalStateException("Failed to create download directory: $absolutePath")
            }
        }

    suspend fun downloadProjectAssets(projectId: String): ListenableWorker.Result {
        Timber.d("Starting work for project: $projectId")
        val project =
            projectDao.findById(projectId) ?: run {
                Timber.e("Project not found: $projectId")
                return ListenableWorker.Result.failure()
            }

        val allAssets = MotionAssetExtractor.extractAssets(context, project.sdui)

        val remoteAssets =
            allAssets.filter { asset ->
                val uri = asset.getUri()
                (uri.scheme == "http") || (uri.scheme == "https")
            }

        val otherAssets =
            allAssets.filter { asset ->
                val uri = asset.getUri()
                (uri.scheme != "http") && (uri.scheme != "https")
            }

        Timber.d("Extracted ${remoteAssets.size} remote assets and ${otherAssets.size} other assets for project: $projectId")

        // 1. Prepare other assets (like TTS)
        var allOthersPrepared = true
        otherAssets.forEach { asset ->
            if (!asset.isPrepared(context, assetManager)) {
                Timber.d("Preparing asset: ${asset.getUri()}")
                if (!asset.prepare(context)) {
                    Timber.e("Failed to prepare asset: ${asset.getUri()}")
                    allOthersPrepared = false
                }
            }
        }

        if (!allOthersPrepared) {
            return ListenableWorker.Result.retry()
        }

        // 2. Handle remote assets
        if (remoteAssets.isEmpty()) {
            Timber.w("No remote assets, Exiting as success")
            return ListenableWorker.Result.success()
        }

        val downloadIds = mutableListOf<Int>()

        remoteAssets.forEach { asset ->
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
            return ListenableWorker.Result.success()
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
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error waiting for downloads")
            ListenableWorker.Result.retry()
        }
    }
}
