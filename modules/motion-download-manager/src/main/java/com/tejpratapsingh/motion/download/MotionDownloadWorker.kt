package com.tejpratapsingh.motion.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import com.ketch.Ketch
import com.ketch.Status
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.io.File
import java.util.UUID

class MotionDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val kv by lazy {
        MMKV.initialize(applicationContext)
        MMKV.mmkvWithID("motion_download_cache")
    }
    private val ketch = Ketch.builder().build(applicationContext)
    private val downloadDir = File(applicationContext.cacheDir, "downloads").apply {
        if (!exists() && !mkdirs()) {
            throw IllegalStateException("Failed to create download directory: $absolutePath")
        }
    }

    override suspend fun doWork(): Result {
        val sduiJson = inputData.getString(KEY_SDUI_JSON) ?: return Result.failure()
        val urls = MotionDownloadManager.extractUrls(sduiJson)

        if (urls.isEmpty()) {
            return Result.success()
        }

        val downloadIds = mutableListOf<Int>()

        urls.forEach { url ->
            val deterministicName = UUID.nameUUIDFromBytes(url.toByteArray()).toString()
            val path = url.substringBefore("?").substringBefore("#")
            val extension = path.substringAfterLast('/', "")
                .substringAfterLast('.', "")
                .let { if (it.length in 2..4 && it.all(Char::isLetterOrDigit)) ".$it" else "" }
            val finalFileName = deterministicName + extension
            val cachedPath = kv.decodeString(url)

            if (cachedPath == null || !File(cachedPath).exists() || !File(cachedPath).isFile) {
                val id = ketch.download(
                    url = url,
                    path = downloadDir.absolutePath,
                    fileName = finalFileName
                )
                downloadIds.add(id)
            }
        }

        if (downloadIds.isEmpty()) {
            return Result.success()
        }

        // Wait for all downloads to finish
        return try {
            ketch.observeDownloads().first { downloads ->
                val relevantDownloads = downloads.filter { it.id in downloadIds }
                
                // Update MMKV for successful downloads
                relevantDownloads.forEach { model ->
                    if (model.status == Status.SUCCESS) {
                        val fullPath = File(model.path, model.fileName).absolutePath
                        kv.encode(model.url, fullPath)
                    }
                }
                
                val allFinished = relevantDownloads.all { it.status == Status.SUCCESS || it.status == Status.FAILED }
                allFinished && relevantDownloads.isNotEmpty()
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error waiting for downloads")
            Result.retry()
        }
    }

    companion object {
        const val KEY_SDUI_JSON = "key_sdui_json"
    }
}
