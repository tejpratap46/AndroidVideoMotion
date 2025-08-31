package com.tejpratapsingh.motionlib.ffmpeg.video

import android.content.Context
import android.graphics.Bitmap
import android.widget.FrameLayout
import android.widget.ImageView
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.loadBitmapsFromDirectory
import com.tejpratapsingh.motionlib.core.extensions.md5
import com.tejpratapsingh.motionlib.ffmpeg.utils.extractFramesFromVideo
import java.io.File

class FFMpegVideoFrameView(
    context: Context,
    val videoFile: File,
    override val startFrame: Int,
    override val endFrame: Int,
    override val loop: Pair<Int, Int> = Pair(0, 0)
) : FrameLayout(context), MotionView {

    override lateinit var motionConfig: MotionConfig

    val imageView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    private val videoBitmaps = extractFramesFromVideo(
        context = context, videoFile = videoFile, outputDirName = videoFile.name.md5()
    ).let {
        context.loadBitmapsFromDirectory(it)
    }

    init {
        addView(imageView)
    }

    private lateinit var currentFrameBitmap: Bitmap

    override fun forFrame(frame: Int): MotionView {
        currentFrameBitmap = videoBitmaps.getOrNull(frame - startFrame) ?: videoBitmaps.last()
        imageView.setImageBitmap(
            currentFrameBitmap
        )
        return this
    }

    override fun getViewBitmap(): Bitmap = currentFrameBitmap
}