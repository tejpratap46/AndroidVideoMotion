package com.tejpratapsingh.motionlib.media3

import android.content.Context
import android.graphics.Bitmap
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.compressToBitmap
import com.tejpratapsingh.motionlib.core.extensions.saveBitmapToCacheFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import timber.log.Timber
import java.io.File
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * Handles frame generation and caching for video production.
 */
class FrameProcessor(
    private val subDirName: String
) {
    /**
     * Renders frames from [motionComposerView] and saves them to the cache directory.
     */
    suspend fun writeFramesToCache(
        context: Context,
        motionComposerView: MotionView,
        motionConfig: MotionConfig,
        totalFrames: Int,
        progressListener: (suspend (Int, Bitmap) -> Unit)?,
    ) {
        val framesProcessed = AtomicInteger(0)
        coroutineScope {
            val semaphore = Semaphore(4) // Limit parallel storage tasks to avoid OOM
            val jobs = mutableListOf<Job>()
            for (i in 1..totalFrames) {
                Timber.d("FrameProcessor: rendering frame $i")
                val frameViewBitmap: Bitmap = motionComposerView.forFrame(i).getViewBitmap()
                val job =
                    launch(Dispatchers.IO) {
                        semaphore.withPermit {
                            val frameBitmap: Bitmap = frameViewBitmap.compressToBitmap(motionConfig.outputQuality)
                            frameViewBitmap.recycle()
                            try {
                                context.saveBitmapToCacheFolder(
                                    frameBitmap,
                                    subDirName,
                                    String.format(Locale.getDefault(), "%05d.png", i),
                                )
                            } catch (e: Exception) {
                                Timber.e(e, "Error saving frame $i: ${e.message}")
                            }
                            progressListener?.let {
                                it(framesProcessed.incrementAndGet(), frameBitmap)
                            }
                            frameBitmap.recycle()
                        }
                    }
                jobs.add(job)
            }
            jobs.joinAll()
        }
    }
}
