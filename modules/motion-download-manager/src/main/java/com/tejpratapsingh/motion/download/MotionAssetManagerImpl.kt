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
import com.tejpratapsingh.motion.sdui.infra.MotionAssetExtractor
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionAssetManager
import com.tejpratapsingh.motionstore.tables.MotionProject
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import timber.log.Timber
import java.io.File

class MotionAssetManagerImpl(
    private val context: Context,
    private val ketch: Ketch,
    private val kv: MMKV,
) : MotionAssetManager {
    override fun getCachedUri(asset: MotionAsset): Uri? {
        return asset.getCachedUri(context, this)
    }

    override fun getLocalPath(asset: MotionAsset): String? {
        val remoteUri = asset.getUri()
        val path = kv.decodeString(remoteUri.toString())
        return if (path != null && File(path).exists() && File(path).isFile) {
            Timber.d("Cache hit for asset: $remoteUri")
            path
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

    override fun deleteCachedAsset(asset: MotionAsset) {
        val remoteUrl = asset.getUri().toString()
        val path = kv.decodeString(remoteUrl)
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                Timber.d("Deleting cached file: $path")
                file.delete()
            }
        }
        kv.removeValueForKey(remoteUrl)
    }

    override fun clearAll() {
        Timber.d("Clearing all cached assets")
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
     * Finds all assets in [project], downloads them using Ketch via WorkManager,
     * and returns a flow of [DownloadProgress].
     */
    fun downloadAssets(project: MotionProject): Flow<DownloadProgress> =
        flow {
            Timber.d("Starting asset download for project: ${project.id}")
            val assets =
                MotionAssetExtractor
                    .extractAssets(context, project.sdui)
                    .filter { asset ->
                        val uri = asset.getUri()
                        uri.scheme == "http" || uri.scheme == "https"
                    }
            val total = assets.size

            if (total == 0) {
                Timber.d("No remote assets to download for project: ${project.id}")
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

            Timber.d("Found $total remote assets to download/verify")

            val urls = assets.map { it.getUri().toString() }

            // Enqueue Worker
            MotionDownloadWorker.enqueueWork(context = context, project = project)

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
                                    fileName = url.substringBefore("?").substringBefore("#").substringAfterLast("/"),
                                    progress = 100,
                                    status = Status.SUCCESS.name,
                                )
                            } else {
                                AssetDownloadProgress(
                                    id = url.hashCode(),
                                    url = url,
                                    fileName = url.substringBefore("?").substringBefore("#").substringAfterLast("/"),
                                    progress = 0,
                                    status = Status.QUEUED.name,
                                )
                            }
                        }

                    val totalProgress = assetProgressList.sumOf { it.progress } / total
                    val downloadedCount =
                        assetProgressList.count { it.status == Status.SUCCESS.name }
                    val failedCount = assetProgressList.count { it.status == Status.FAILED.name }

                    val isComplete = downloadedCount + failedCount == total

                    if (isComplete) {
                        Timber.d("Download complete for project: ${project.id}. Success: $downloadedCount, Failed: $failedCount")
                    }

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

    fun hasPendingDownloads(project: MotionProject): Boolean {
        val assets =
            MotionAssetExtractor
                .extractAssets(context, project.sdui)
                .filter { asset ->
                    val uri = asset.getUri()
                    uri.scheme == "http" || uri.scheme == "https"
                }
        if (assets.isEmpty()) return false

        return assets.any { asset ->
            val url = asset.getUri().toString()
            val cachedPath = kv.decodeString(url)
            cachedPath == null || !File(cachedPath).exists() || !File(cachedPath).isFile
        }
    }

    companion object {
    }
}
