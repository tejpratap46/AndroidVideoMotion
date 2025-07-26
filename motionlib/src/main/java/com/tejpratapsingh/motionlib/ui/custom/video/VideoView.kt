package com.tejpratapsingh.motionlib.ui.custom.video

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.extensions.downloadFile // Assuming this handles async download
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jcodec.api.FrameGrab
import org.jcodec.api.android.AndroidFrameGrab
import org.jcodec.common.AndroidUtil
import org.jcodec.common.io.IOUtils
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Picture
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class VideoView(
    context: Context,
    startFrame: Int,
    endFrame: Int
) : MotionView(
    context = context,
    startFrame = startFrame,
    endFrame = endFrame
) {

    private companion object {
        private const val TAG = "CustomVideoView"
    }

    private var retriever: MediaMetadataRetriever? = null
    private var frameGrab: FrameGrab? = null
    private var videoFile: File? = null
    private var tempVideoFile: File? = null // For files created by this view

    // Scope for background tasks related to this view
    private val viewScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var downloadJob: Job? = null

    private val imageView: ImageView = ImageView(context).apply {
        // Consider setting a placeholder image initially
        // layoutBy should be called after the view is added to a parent
        // or ensure parent is available.
        // For simplicity, assuming layoutBy is handled correctly elsewhere or
        // you're adding this view programmatically and then calling layoutBy.
    }

    init {
        // It's generally better to add views in onAttachedToWindow or similar
        // if this VideoView itself is a ViewGroup.
        // If it's just a proxy to manage an ImageView, ensure imageView is added
        // to this MotionView's hierarchy if MotionView is a ViewGroup.
        // If MotionView isn't a ViewGroup, this imageView needs to be handled
        // by the parent of VideoView.
        // For now, let's assume `imageView` is the view to be returned by `forFrame`.
    }


    fun setVideoFromAssets(fileName: String): VideoView {
        viewScope.launch(Dispatchers.IO) {
            try {
                val tempFile = File.createTempFile("assetVideo_", ".mp4", context.cacheDir)
                val outputStream: OutputStream = FileOutputStream(tempFile)
                context.assets.open(fileName).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
                IOUtils.closeQuietly(outputStream)
                withContext(Dispatchers.Main) {
                    videoFile = tempFile
                    tempVideoFile = tempFile // Mark for deletion
                    initializeMediaSources()
                    // Potentially trigger a refresh or notify that video is ready
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error setting video from assets: $fileName", e)
                // Handle error (e.g., show error state)
            }
        }
        return this
    }

    fun setVideoFromUrl(uri: Uri, httpClient: HttpClient): VideoView {
        downloadJob?.cancel() // Cancel any previous download
        downloadJob = viewScope.launch {
            try {
                val tempFile = File.createTempFile("urlVideo_", ".mp4", context.cacheDir)
                // Assuming downloadFile is a suspend function and handles exceptions
                httpClient.downloadFile(file = tempFile, url = uri.toString())
                videoFile = tempFile
                tempVideoFile = tempFile // Mark for deletion
                initializeMediaSources()
                // Potentially trigger a refresh or notify that video is ready
            } catch (e: Exception) { // Catch more specific exceptions from downloadFile
                Log.e(TAG, "Error downloading video from URL: $uri", e)
                // Handle error
            }
        }
        return this
    }

    private fun initializeMediaSources() {
        releaseMediaSources() // Release any existing sources first

        val currentVideoFile = videoFile ?: return // Exit if no video file is set

        try {
            retriever = MediaMetadataRetriever().apply {
                setDataSource(currentVideoFile.absolutePath)
            }
            // FrameGrab might be more efficient for sequential frame access
            // but MediaMetadataRetriever is simpler for random access by index.
            // Choose based on your primary use case.
            frameGrab = AndroidFrameGrab.createFrameGrab(NIOUtils.readableChannel(currentVideoFile))
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing media sources for: ${currentVideoFile.path}", e)
            retriever?.release()
            retriever = null
            // frameGrab channel needs to be closed if open
            // Consider how NIOUtils.readableChannel needs to be closed if FrameGrab fails.
            // JCodec's FrameGrab doesn't have an explicit close(), relies on channel closure.
        }
    }


    override fun forFrame(frame: Int): View {
        super.forFrame(frame) // Call super if it does something important

        val currentRetriever = retriever
        val currentFrameGrab = frameGrab

        if (videoFile == null) {
            // No video loaded, show placeholder or return empty view
            // imageView.setImageDrawable(null) // Or a placeholder
            return imageView // Or this, depending on how MotionView works
        }

        // Ensure media sources are initialized if videoFile is set but they aren't
        if (videoFile != null && (currentRetriever == null || currentFrameGrab == null)) {
            Log.w(TAG, "Media sources not initialized in forFrame, attempting re-init.")
            initializeMediaSources() // Try to re-initialize
            // If re-init fails, currentRetriever/currentFrameGrab will still be null
            if (retriever == null || frameGrab == null) {
                Log.e(TAG, "Failed to initialize media sources in forFrame for ${videoFile?.path}")
                // imageView.setImageDrawable(error_placeholder)
                return imageView
            }
        }


        try {
            val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // MediaMetadataRetriever.getFrameAtIndex is 0-indexed for frames.
                // Ensure your 'frame' variable aligns with this or adjust.
                // It can throw IllegalArgumentException if frame is out of bounds.
                currentRetriever?.getFrameAtIndex(frame)
            } else if (currentFrameGrab != null) {
                // FrameGrab.seekToFramePrecise might be 0-indexed or 1-indexed, check docs.
                // It also might be slow for random access.
                val picture: Picture? = currentFrameGrab.seekToFramePrecise(frame)?.nativeFrame
                picture?.let { AndroidUtil.toBitmap(it) }
            } else {
                null
            }

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                Log.w(TAG, "Could not get frame $frame for ${videoFile?.path}")
                // Optionally set a placeholder or keep the last frame
            }
        } catch (ex: Exception) { // Catch more specific exceptions
            Log.e(TAG, "Error getting frame $frame for ${videoFile?.path}", ex)
            // Optionally set an error placeholder on imageView
        }

        return imageView // Return the ImageView that now holds the frame
    }

    fun release() {
        viewScope.cancel() // Cancel ongoing coroutines
        downloadJob?.cancel()
        releaseMediaSources()

        tempVideoFile?.let {
            if (it.exists()) {
                it.delete()
                Log.d(TAG, "Deleted temp video file: ${it.path}")
            }
            tempVideoFile = null
        }
        videoFile = null // Clear reference, especially if it was a temp file
    }

    private fun releaseMediaSources() {
        try {
            retriever?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaMetadataRetriever", e)
        }
        retriever = null

        // JCodec's FrameGrab doesn't have a direct close/release method.
        // It operates on a ReadableByteChannel. If you created the channel
        // yourself and passed it to FrameGrab, you'd close that channel.
        // NIOUtils.readableChannel(file) likely needs its underlying resources closed.
        // However, AndroidFrameGrab.createFrameGrab might manage this.
        // For safety, if you have access to the channel that was passed to frameGrab, close it.
        // Since AndroidFrameGrab likely creates its own internal channel from the file,
        // there might not be an explicit step here unless JCodec docs say otherwise.
        // For now, just nullifying it.
        frameGrab = null
        Log.d(TAG, "Media sources released")
    }

    // Call release() when the view is no longer needed, e.g.,
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }
}