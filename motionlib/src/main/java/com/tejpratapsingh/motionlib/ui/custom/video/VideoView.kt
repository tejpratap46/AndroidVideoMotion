package com.tejpratapsingh.motionlib.ui.custom.video

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.ImageView
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.extensions.downloadFile
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import org.jcodec.api.FrameGrab
import org.jcodec.api.android.AndroidFrameGrab
import org.jcodec.common.AndroidUtil
import org.jcodec.common.io.IOUtils
import org.jcodec.common.io.NIOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class VideoView(
    context: Context,
    startFrame: Int,
    endFrame: Int
) :
    MotionView(
        context = context,
        startFrame = startFrame,
        endFrame = endFrame
    ) {

    private lateinit var retriever: MediaMetadataRetriever
    private lateinit var frameGrab: FrameGrab
    private lateinit var httpClient: HttpClient
    private lateinit var httpVideoUri: Uri
    private lateinit var videoFile: File

    constructor(
        context: Context,
        startFrame: Int,
        endFrame: Int, videoFile: File
    ) : this(context, startFrame, endFrame) {
        retriever.setDataSource(videoFile.absolutePath)
        frameGrab = AndroidFrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile))
    }

    private val imageView: ImageView = ImageView(context)

    init {
        imageView.layoutBy(
            x = leftTo {
                parent.left()
            }.rightTo {
                parent.right()
            },
            y = topTo {
                parent.top()
            }.bottomTo {
                parent.bottom()
            }
        )
    }

    fun setVideoFromAssets(fileName: String): VideoView {
        videoFile = File.createTempFile("testVideo", "mp4")
        val outputStream: OutputStream = FileOutputStream(videoFile)
        outputStream.write(context.assets.open(fileName).readBytes())
        IOUtils.closeQuietly(outputStream)

        return this
    }

    fun setVideoFromUrl(uri: Uri, httpClient: HttpClient): VideoView {
        this@VideoView.httpClient = httpClient
        this@VideoView.httpVideoUri = uri
        return this@VideoView
    }

    override fun forFrame(frame: Int): View {
        super.forFrame(frame)

        if (!::retriever.isInitialized || !::frameGrab.isInitialized) {
            // If video frames are not created
            if (::httpClient.isInitialized && ::httpVideoUri.isInitialized && !::videoFile.isInitialized) {
                runBlocking {
                    videoFile = File.createTempFile("testVideo", "mp4")

                    videoFile =
                        httpClient.downloadFile(file = videoFile, url = httpVideoUri.toString())
                }
            }

            retriever = MediaMetadataRetriever().apply {
                setDataSource(videoFile.absolutePath)
            }
            frameGrab = AndroidFrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile))
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                imageView.setImageBitmap(retriever.getFrameAtIndex(frame))
            } else if (this::frameGrab.isInitialized) {
                imageView.setImageBitmap(AndroidUtil.toBitmap(frameGrab.seekToFramePrecise(frame).nativeFrame))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return this
    }
}