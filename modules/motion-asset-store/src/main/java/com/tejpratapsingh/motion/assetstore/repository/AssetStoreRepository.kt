package com.tejpratapsingh.motion.assetstore.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.ketch.Ketch
import com.ketch.Status
import com.tejpratapsingh.motion.assetstore.model.AssetFileType
import com.tejpratapsingh.motion.assetstore.model.AssetItem
import com.tejpratapsingh.motion.assetstore.model.AssetSearchResult
import com.tejpratapsingh.motion.assetstore.service.ExternalAssetService
import com.tejpratapsingh.motionlib.assettype.ImageAsset
import com.tejpratapsingh.motionlib.assettype.SimpleMotionAsset
import com.tejpratapsingh.motionlib.assettype.VideoAsset
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionAssetManager
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import timber.log.Timber
import java.io.File
import java.util.UUID

class AssetStoreRepository(
    private val context: Context,
    private val services: List<ExternalAssetService>,
    private val ketch: Ketch,
    private val kv: MMKV,
    private val assetManager: MotionAssetManager,
) {
    private val downloadDir =
        File(context.cacheDir, "downloads").apply {
            if (!exists()) {
                mkdirs()
            }
        }

    fun getService(id: String = "pexels"): ExternalAssetService? =
        services.find { it.serviceId == id } ?: services.firstOrNull()

    suspend fun searchImages(
        query: String,
        page: Int = 1,
        serviceId: String = "pexels",
    ): Result<AssetSearchResult<AssetItem.Image>> {
        val service = getService(serviceId)
            ?: return Result.failure(IllegalStateException("No external asset service registered"))

        return if (query.isBlank()) {
            service.getCuratedImages(page = page)
        } else {
            service.searchImages(query = query, page = page)
        }
    }

    suspend fun searchVideos(
        query: String,
        page: Int = 1,
        serviceId: String = "pexels",
    ): Result<AssetSearchResult<AssetItem.Video>> {
        val service = getService(serviceId)
            ?: return Result.failure(IllegalStateException("No external asset service registered"))

        return if (query.isBlank()) {
            service.getPopularVideos(page = page)
        } else {
            service.searchVideos(query = query, page = page)
        }
    }

    /**
     * Checks if [url] is already cached locally. Returns local file URI if cached.
     */
    fun getCachedUri(url: String): Uri? {
        val cachedPath = kv.decodeString(url)
        return if (cachedPath != null && File(cachedPath).exists() && File(cachedPath).isFile) {
            Uri.fromFile(File(cachedPath))
        } else {
            null
        }
    }

    /**
     * Enqueues download for a remote asset URL and emits progress flow (0 to 100).
     * Automatically updates MMKV cache when complete.
     */
    fun downloadAndCacheAsset(url: String): Flow<Int> =
        flow {
            val cachedUri = getCachedUri(url)
            if (cachedUri != null) {
                emit(100)
                return@flow
            }

            val deterministicName = UUID.nameUUIDFromBytes(url.toByteArray()).toString()
            val extension =
                MimeTypeMap.getFileExtensionFromUrl(url).let {
                    if (it.isNotEmpty()) ".$it" else ""
                }
            val finalFileName = deterministicName + extension

            val downloadId =
                ketch.download(
                    url = url,
                    path = downloadDir.absolutePath,
                    fileName = finalFileName,
                )

            ketch
                .observeDownloads()
                .map { downloads ->
                    downloads.find { it.id == downloadId }
                }.transformWhile { model ->
                    if (model == null) {
                        emit(0)
                        true
                    } else {
                        emit(model.progress)
                        if (model.status == Status.SUCCESS) {
                            val fullPath = File(model.path, model.fileName).absolutePath
                            kv.encode(url, fullPath)
                            Timber.d("Download succeeded and cached in MMKV: $url -> $fullPath")
                            false
                        } else if (model.status == Status.FAILED) {
                            Timber.e("Download failed for url: $url")
                            false
                        } else {
                            true
                        }
                    }
                }.collect { emit(it) }
        }

    /**
     * Converts an [AssetItem] or URI string into a typed [MotionAsset].
     */
    fun createMotionAsset(
        uriString: String,
        fileType: AssetFileType,
    ): MotionAsset {
        val uri = Uri.parse(uriString)
        return when (fileType) {
            AssetFileType.IMAGE -> ImageAsset(uri)
            AssetFileType.VIDEO -> VideoAsset(uri)
        }
    }
}
