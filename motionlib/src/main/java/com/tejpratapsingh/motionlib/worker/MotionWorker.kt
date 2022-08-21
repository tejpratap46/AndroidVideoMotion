package com.tejpratapsingh.motionlib.worker

import android.content.Context
import android.graphics.Bitmap
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tejpratapsingh.motionlib.core.MotionVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

abstract class MotionWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    companion object {
        private const val TAG = "MotionWorker"

        val WORK_ID: Int by lazy {
            (System.currentTimeMillis() / 1000L).toInt()
        }
    }

    private val mMotionVideo: MotionVideo by lazy {
        this.getMotionVideo()
    }

    override suspend fun doWork(): Result {
        val videoFile: File = generateVideo(
            motionVideo = mMotionVideo,
            progressListener = { progress, bitmap ->
                this.onProgress(
                    totalFrames = mMotionVideo.totalFrames,
                    progress = progress, bitmap = bitmap
                )
            }
        )
        this.onCompleted(videoFile = videoFile)
        return Result.success()
    }

    abstract fun getMotionVideo(): MotionVideo

    abstract fun onProgress(totalFrames: Int, progress: Int, bitmap: Bitmap)

    abstract fun onCompleted(videoFile: File)

    private suspend fun generateVideo(
        motionVideo: MotionVideo,
        progressListener: ((progress: Int, bitmap: Bitmap) -> Unit)?
    ): File =
        withContext(Dispatchers.IO) {
            return@withContext motionVideo.produceVideo(
                outputFile = File.createTempFile(
                    "out",
                    ".mp4",
                    applicationContext.filesDir
                ),
                progressListener = progressListener
            )
        }
}