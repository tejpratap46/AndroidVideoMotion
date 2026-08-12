package com.tejpratapsingh.motionlib.ui.custom.video

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.core.graphics.createBitmap
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.toBitmap
import com.tejpratapsingh.motionlib.core.infra.VideoFrameHandler
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import kotlinx.coroutines.runBlocking

class MediaFrameView(
    context: Context,
    val asset: MotionAsset,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
) : BaseContourMotionView(context, startFrame, endFrame, effects = effects) {
    /**
     * For backward compatibility, the video URI.
     */
    val videoUri: Uri get() = asset.getUri()

    private val imageView =
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

    private var handler: VideoFrameHandler
    private lateinit var currentFrameBitmap: Bitmap

    init {
        imageView.layoutBy(
            x = leftTo { parent.left() }.rightTo { parent.right() },
            y = topTo { parent.top() }.bottomTo { parent.bottom() },
        )

        runBlocking {
            handler = VideoFrameHandler.create(context, videoUri)
        }
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        runBlocking {
            currentFrameBitmap =
                handler.seekToFrame(frame.toLong()).let {
                    handler.currentFrameBitmap() ?: createBitmap(imageView.width, imageView.height)
                }
        }
        imageView.setImageBitmap(
            currentFrameBitmap,
        )
        return this
    }

    override fun getViewBitmap(): Bitmap = this.toBitmap()

    override val assets: List<MotionAsset>
        get() = listOf(asset)
}
