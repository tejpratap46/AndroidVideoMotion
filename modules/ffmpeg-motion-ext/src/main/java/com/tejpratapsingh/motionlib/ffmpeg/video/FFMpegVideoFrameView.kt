package com.tejpratapsingh.motionlib.ffmpeg.video

import android.content.Context
import android.graphics.Bitmap
import android.widget.FrameLayout
import android.widget.ImageView
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.loadBitmapsFromDirectory
import com.tejpratapsingh.motionlib.core.extensions.md5
import com.tejpratapsingh.motionlib.core.findConfig
import com.tejpratapsingh.motionlib.ffmpeg.utils.extractFramesFromVideo
import java.io.File

class FFMpegVideoFrameView(
    context: Context,
    val asset: MotionAsset,
    override val startFrame: Int,
    override val endFrame: Int,
    override var loop: Pair<Int, Int> = Pair(0, 0),
    effects: List<MotionEffect> = emptyList(),
) : FrameLayout(context),
    MotionView {
    override val motionConfig: MotionConfig by lazy { findConfig() }

    /**
     * For backward compatibility, the video file.
     */
    val videoFile: File get() = File(asset.getUri().path!!)

    override val effects: MutableList<MotionEffect> = mutableListOf()

    override fun addEffect(effect: MotionEffect) {
        effect.motionView = this
        effects.add(effect)
    }

    init {
        effects.forEach { addEffect(it) }
    }

    val imageView =
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

    private val videoBitmaps =
        extractFramesFromVideo(
            context = context,
            videoFile = videoFile,
            outputDirName = videoFile.name.md5(),
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
            currentFrameBitmap,
        )
        effects.forEach { it.forFrame(frame) }
        return this
    }

    override fun getViewBitmap(): Bitmap = currentFrameBitmap

    override val assets: List<MotionAsset>
        get() = listOf(asset)
}
