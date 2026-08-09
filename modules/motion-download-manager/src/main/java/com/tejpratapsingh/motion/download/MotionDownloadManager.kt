package com.tejpratapsingh.motion.download

import android.content.Context
import android.net.Uri
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ketch.Ketch
import com.ketch.Status
import com.tejpratapsingh.motion.download.model.AssetDownloadProgress
import com.tejpratapsingh.motion.download.model.DownloadProgress
import com.tejpratapsingh.motionlib.core.MotionCacheManager
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import java.io.File

class MotionDownloadManager(
    private val context: Context,
) : MotionCacheManager {
    init {
        MMKV.initialize(context)
    }

    private val kv = MMKV.mmkvWithID("motion_download_cache")
    private val ketch = Ketch.builder().build(context)

    override fun getCachedUri(remoteUri: Uri): Uri? {
        val path = kv.decodeString(remoteUri.toString())
        return if (path != null && File(path).exists() && File(path).isFile) {
            Uri.fromFile(File(path))
        } else {
            null
        }
    }

    override fun getAllCachedAssets(): Map<String, String> {
        val allKeys = kv.allKeys() ?: return emptyMap()
        val assets = mutableMapOf<String, String>()
        allKeys.forEach { key ->
            val path = kv.decodeString(key)
            if (path != null && File(path).isFile) {
                assets[key] = path
            } else {
                kv.removeValueForKey(key)
            }
        }
        return assets
    }

    override fun deleteCachedAsset(remoteUrl: String) {
        val path = kv.decodeString(remoteUrl)
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
        kv.removeValueForKey(remoteUrl)
    }

    override fun clearAll() {
        val allKeys = kv.allKeys() ?: return
        allKeys.forEach { key ->
            val path = kv.decodeString(key)
            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
        }
        kv.clearAll()
    }

    /**
     * Finds all HTTP/HTTPS links in [sduiJson], downloads them using Ketch via WorkManager,
     * and returns a flow of [DownloadProgress].
     */
    fun downloadAssets(sduiJson: String): Flow<DownloadProgress> =
        flow {
            val urls = extractUrls(sduiJson)
            val total = urls.size

            if (total == 0) {
                emit(
                    DownloadProgress(
                        totalFiles = 0,
                        downloadedFiles = 0,
                        currentProgress = 100,
                        isComplete = true,
                    ),
                )
                return@flow
            }

            // Enqueue Worker
            val workRequest =
                OneTimeWorkRequestBuilder<MotionDownloadWorker>()
                    .setInputData(workDataOf(MotionDownloadWorker.KEY_SDUI_JSON to sduiJson))
                    .build()
            WorkManager.getInstance(context).enqueue(workRequest)

            // Observe Ketch progress
            ketch
                .observeDownloads()
                .map { downloads ->
                    val relevantDownloads = downloads.filter { it.url in urls }

                    // Also need to account for already cached files
                    val cachedUrls =
                        urls.filter { url ->
                            val cachedPath = kv.decodeString(url)
                            cachedPath != null && File(cachedPath).exists() && File(cachedPath).isFile
                        }

                    val assetProgressList =
                        urls.map { url ->
                            val model = relevantDownloads.find { it.url == url }
                            if (model != null) {
                                AssetDownloadProgress(
                                    id = model.id,
                                    url = url,
                                    fileName = model.fileName,
                                    progress = model.progress,
                                    status = model.status.name,
                                    error = if (model.status == Status.FAILED) model.failureReason else null,
                                )
                            } else if (url in cachedUrls) {
                                AssetDownloadProgress(
                                    id = url.hashCode(),
                                    url = url,
                                    fileName = url.substringAfterLast("/"),
                                    progress = 100,
                                    status = Status.SUCCESS.name,
                                )
                            } else {
                                AssetDownloadProgress(
                                    id = url.hashCode(),
                                    url = url,
                                    fileName = url.substringAfterLast("/"),
                                    progress = 0,
                                    status = Status.QUEUED.name,
                                )
                            }
                        }

                    val totalProgress = if (total > 0) assetProgressList.sumOf { it.progress } / total else 0
                    val downloadedCount =
                        assetProgressList.count { it.status == Status.SUCCESS.name }
                    val failedCount = assetProgressList.count { it.status == Status.FAILED.name }

                    val isComplete = downloadedCount + failedCount == total

                    DownloadProgress(
                        totalFiles = total,
                        downloadedFiles = downloadedCount,
                        currentProgress = totalProgress,
                        assetProgressList = assetProgressList,
                        isComplete = isComplete,
                        error = if (failedCount > 0) "$failedCount files failed to download" else null,
                    )
                }.transformWhile { progress ->
                    emit(progress)
                    !progress.isComplete
                }.collect { emit(it) }
        }

    fun retryAsset(id: Int) {
        ketch.retry(id)
    }

    fun hasPendingDownloads(sduiJson: String): Boolean {
        val urls = extractUrls(sduiJson)
        if (urls.isEmpty()) return false

        return urls.any { url ->
            val cachedPath = kv.decodeString(url)
            cachedPath == null || !File(cachedPath).exists() || !File(cachedPath).isFile
        }
    }

    companion object {
        fun extractUrls(sduiJson: String): List<String> {
            val urlRegex = """https?://[^\s"'<>{}|\\^`\[\]]+""".toRegex()
            return urlRegex
                .findAll(sduiJson)
                .map { it.value }
                .toSet()
                .toList()
        }
    }
}
