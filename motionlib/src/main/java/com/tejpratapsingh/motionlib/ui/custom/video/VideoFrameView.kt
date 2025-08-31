package com.tejpratapsingh.motionlib.ui.custom.video

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.BaseMotionView
import com.tejpratapsingh.motionlib.utils.extractAllVideoFrames
import com.tejpratapsingh.motionlib.utils.getVideoFpsWithRetriever
import kotlin.math.roundToLong

class VideoFrameView(
    context: Context, videoUri: Uri, startFrame: Int, endFrame: Int
) : BaseMotionView(context, startFrame, endFrame) {

    val fps = getVideoFpsWithRetriever(context, videoUri) ?: 30F
    val videoBitmaps = extractAllVideoFrames(
        context = context, videoUri = videoUri, frameIntervalUs = (1_000_000 / fps).roundToLong()
    )

    private val imageView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
    }

    init {
        imageView.layoutBy(
            x = leftTo { parent.left() }.rightTo { parent.right() },
            y = topTo { parent.top() }.bottomTo { parent.bottom() })
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        contourHeightOf {
            motionConfig.height.toYInt()
        }
        contourWidthOf {
            motionConfig.width.toXInt()
        }
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