package com.tejpratapsingh.motionlib.ui.custom.image

import android.content.Context
import android.net.Uri
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import com.tejpratapsingh.motionlib.core.MotionCacheManager
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionlib.utils.ImageUtil
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * A [com.tejpratapsingh.motionlib.core.MotionView] that displays a static image from a [android.net.Uri].
 * Similar to [com.tejpratapsingh.motionlib.ui.custom.video.MotionVideoPlayer] but for static images.
 */
class MotionImageView(
    context: Context,
    val imageUri: Uri,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
) : BaseContourMotionView(context, startFrame, endFrame, effects = effects),
    KoinComponent {
    private val cacheManager: MotionCacheManager by inject()

    private val imageView: ImageView =
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

    private val previewLayout: FrameLayout =
        FrameLayout(context)

    init {
        val previewLayoutParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER,
            )
        previewLayout.addView(imageView, previewLayoutParams)

        previewLayout.layoutBy(
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

        // Load image into ImageView for preview
        runBlocking {
            val localUri = cacheManager.getCachedUri(imageUri) ?: imageUri
            Timber.d("localUri: $localUri")
            val bitmap = ImageUtil.fetchBitmap(context, localUri)
            imageView.setImageBitmap(bitmap)
        }
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        return this
    }

    override fun isCached(cacheManager: MotionCacheManager): Boolean =
        if (imageUri.scheme == "http" || imageUri.scheme == "https") {
            cacheManager.isCached(imageUri)
        } else {
            true
        }
}
