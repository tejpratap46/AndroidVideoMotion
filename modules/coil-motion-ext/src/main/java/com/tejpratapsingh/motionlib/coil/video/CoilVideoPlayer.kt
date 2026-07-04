package com.tejpratapsingh.motionlib.coil.video

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.ImageView
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import kotlinx.coroutines.runBlocking

/**
 * A [MotionView] that displays video frames using Coil's video extension.
 * Uses [videoFrameMillis] to select specific frames based on time.
 */
class CoilVideoPlayer(
    context: Context,
    val videoUri: Uri,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
) : BaseContourMotionView(context, startFrame, endFrame, effects = effects) {
    private val imageView =
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

    private val imageLoader =
        ImageLoader
            .Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }.build()

    init {
        imageView.layoutBy(
            x = leftTo { parent.left() }.rightTo { parent.right() },
            y = topTo { parent.top() }.bottomTo { parent.bottom() },
        )

        contourHeightOf {
            provideCurrentConfig()
                .aspectRatio.height
                .toYInt()
        }
        contourWidthOf {
            provideCurrentConfig()
                .aspectRatio.width
                .toXInt()
        }
    }

    private var currentBitmap: Bitmap? = null

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)

        val relativeFrame = frame - startFrame
        if (relativeFrame < 0) return this

        val fps = provideCurrentConfig().fps
        val timeMillis = (relativeFrame * 1000L) / fps

        // Load frame synchronously for motion processing
        runBlocking {
            val request =
                ImageRequest
                    .Builder(context)
                    .data(videoUri)
                    .videoFrameMillis(timeMillis)
                    .allowHardware(false) // Required for getViewBitmap/transformations
                    .build()

            val result = imageLoader.execute(request)
            val drawable = result.drawable
            if (drawable is BitmapDrawable) {
                currentBitmap = drawable.bitmap
                imageView.setImageBitmap(currentBitmap)
            }
        }

        return this
    }

    override fun getViewBitmap(): Bitmap = currentBitmap ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
}
