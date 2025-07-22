package com.tejpratapsingh.motionlib.worker

import android.content.Context
import android.graphics.Bitmap
import android.util.Log // Added for logging
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf // For progress reporting
import com.tejpratapsingh.motionlib.core.MotionVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

abstract class MotionWorker(appContext: Context, workerParams: WorkerParameters) : // Renamed parameters for clarity
    CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "MotionWorker" // Standard TAG naming
        const val PROGRESS_KEY = "progress"
        const val TOTAL_FRAMES_KEY = "total_frames"
        // WORK_ID is removed as WorkManager provides its own ID (this.id)
        // and unique work can be managed with named WorkRequests.
    }

    // Lazy initialization is good.
    // Consider passing inputData from workerParams if MotionVideo needs it.
    private val mMotionVideo: MotionVideo by lazy {
        getMotionVideo(inputData) // Assuming getMotionVideo now might take WorkParameters' inputData
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker ${this.id}: Starting video generation.")
        return try {
            val videoFile: File = generateVideo(
                motionVideo = mMotionVideo,
                progressListener = { progress, currentBitmap ->
                    // Report progress to WorkManager
                    val progressData = workDataOf(
                        PROGRESS_KEY to progress,
                        TOTAL_FRAMES_KEY to mMotionVideo.totalFrames
                    )
                    setProgressAsync(progressData)

                    // Call the abstract onProgress for more specific handling
                    this.onProgress(
                        totalFrames = mMotionVideo.totalFrames,
                        currentProgress = progress, // Renamed for clarity
                        bitmap = currentBitmap
                    )
                }
            )
            this.onCompleted(videoFile = videoFile)
            Log.d(TAG, "Worker ${this.id}: Video generation successful: ${videoFile.absolutePath}")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker ${this.id}: Error during video generation.", e)
            onFailed(e) // Optional: abstract method for specific failure handling
            Result.failure()
        }
    }

    /**
     * Called to retrieve/create the MotionVideo instance.
     * @param inputData The input data passed to the worker.
     */
    abstract fun getMotionVideo(inputData: androidx.work.Data): MotionVideo

    /**
     * Called on progress update.
     * This is called from a background thread (likely Dispatchers.IO).
     *
     * @param totalFrames The total number of frames to be processed.
     * @param currentProgress The number of frames processed so far.
     * @param bitmap The current frame/bitmap being processed.
     */
    abstract fun onProgress(totalFrames: Int, currentProgress: Int, bitmap: Bitmap)

    /**
     * Called when the video generation is completed successfully.
     * This is called from a background thread.
     *
     * @param videoFile The generated video file.
     */
    abstract fun onCompleted(videoFile: File)

    /**
     * Optional: Called if an exception occurs during doWork.
     * This is called from a background thread.
     *
     * @param exception The exception that occurred.
     */
    open fun onFailed(exception: Exception) {
        // Default implementation does nothing, subclasses can override.
    }

    private suspend fun generateVideo(
        motionVideo: MotionVideo,
        progressListener: ((progress: Int, bitmap: Bitmap) -> Unit)?
    ): File =
        withContext(Dispatchers.IO) {
            val outputFile = File.createTempFile(
                "motion_video_out_", // More descriptive prefix
                ".mp4",
                applicationContext.cacheDir // Use cacheDir for temp files that can be cleared
            )
            Log.d(TAG, "Worker ${this@MotionWorker.id}: Generating video at ${outputFile.absolutePath}")

            // Assuming produceVideo handles its own exceptions or lets them propagate
            return@withContext motionVideo.produceVideo(
                outputFile = outputFile,
                progressListener = progressListener
            )
        }
}
