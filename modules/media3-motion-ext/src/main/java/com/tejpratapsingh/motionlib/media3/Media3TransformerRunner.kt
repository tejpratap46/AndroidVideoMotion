package com.tejpratapsingh.motionlib.media3

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Handles the execution and lifecycle of the Media3 [Transformer].
 */
@UnstableApi
class Media3TransformerRunner {
    /**
     * Exports the [composition] to [outputFile] using Media3 [Transformer].
     */
    suspend fun export(
        context: Context,
        composition: Composition,
        outputFile: File,
    ) {
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val transformer =
                    Transformer
                        .Builder(context)
                        .setVideoMimeType(MimeTypes.VIDEO_H264)
                        .setAudioMimeType(MimeTypes.AUDIO_AAC)
                        .addListener(
                            object : Transformer.Listener {
                                override fun onCompleted(
                                    composition: Composition,
                                    exportResult: ExportResult,
                                ) {
                                    if (continuation.isActive) {
                                        continuation.resume(Unit)
                                    }
                                }

                                override fun onError(
                                    composition: Composition,
                                    exportResult: ExportResult,
                                    exportException: ExportException,
                                ) {
                                    if (outputFile.exists()) {
                                        outputFile.delete()
                                    }
                                    if (continuation.isActive) {
                                        continuation.resumeWithException(exportException)
                                    }
                                }
                            },
                        ).build()

                continuation.invokeOnCancellation {
                    Handler(Looper.getMainLooper()).post {
                        try {
                            transformer.cancel()
                        } catch (e: Exception) {
                            Timber.w(e, "Media3TransformerRunner: Error cancelling transformer")
                        }
                    }
                }
                transformer.start(composition, outputFile.absolutePath)
            }
        }
    }
}
