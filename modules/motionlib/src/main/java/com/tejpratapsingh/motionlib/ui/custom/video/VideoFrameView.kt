package com.tejpratapsingh.motionlib.ui.custom.video

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionAssetManager
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import com.tejpratapsingh.motionlib.utils.extractAllVideoFrames
import com.tejpratapsingh.motionlib.utils.getVideoFpsWithRetriever
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.roundToLong

class VideoFrameView(
    context: Context,
    val asset: MotionAsset,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
) : BaseContourMotionView(context, startFrame, endFrame, effects = effects),
    KoinComponent {
    /**
     * For backward compatibility, the video URI.
     */
    val videoUri: Uri get() = asset.getUri()

    private val cacheManager: MotionAssetManager by inject()
    private val localVideoUri =
        asset.isCached(context, cacheManager).let { isCached ->
            if (isCached) cacheManager.getCachedUri(asset) ?: videoUri else videoUri
        }

    val fps = getVideoFpsWithRetriever(context, localVideoUri) ?: 30F
    val videoBitmaps =
        extractAllVideoFrames(
            context = context,
            videoUri = localVideoUri,
            frameIntervalUs = (1_000_000 / fps).roundToLong(),
        )

    private val imageView =
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

    init {
        imageView.layoutBy(
            x = leftTo { parent.left() }.rightTo { parent.right() },
            y = topTo { parent.top() }.bottomTo { parent.bottom() },
        )
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    private lateinit var currentFrameBitmap: Bitmap

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        currentFrameBitmap = videoBitmaps.getOrNull(frame - startFrame) ?: videoBitmaps.last()
        imageView.setImageBitmap(
            currentFrameBitmap,
        )
        return this
    }

    override fun getViewBitmap(): Bitmap = currentFrameBitmap

    override val assets: List<MotionAsset>
        get() = listOf(asset)
}
