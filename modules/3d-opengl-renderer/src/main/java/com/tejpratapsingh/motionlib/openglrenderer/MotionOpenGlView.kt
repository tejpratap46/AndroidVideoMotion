package com.tejpratapsingh.motionlib.openglrenderer

import android.content.Context
import android.graphics.Bitmap
import android.widget.FrameLayout
import android.widget.ImageView
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.findConfig

class MotionOpenGlView(
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
     * For backward compatibility, the model asset path.
     */
    val modelAssetPath: String get() = asset.getUri().toString().removePrefix("asset:///")

    override val effects: MutableList<MotionEffect> = mutableListOf()

    override fun addEffect(effect: MotionEffect) {
        effect.motionView = this
        effects.add(effect)
    }

    init {
        effects.forEach { addEffect(it) }
    }

    private val imageView =
        ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

    private var offscreenRendererInitialized = false

    private val offscreenRenderer by lazy {
        Object3DToBitmapRenderer(
            context = context,
            assetFileName = modelAssetPath,
            width = motionConfig.aspectRatio.width,
            height = motionConfig.aspectRatio.height,
            objectColor = floatArrayOf(0.7f, 0.3f, 0.3f, 1.0f),
        )
    }

    init {
        // Initialize OpenGL renderer with the model asset path
        addView(imageView)
    }

    override fun forFrame(frame: Int): MotionView {
        if (!offscreenRendererInitialized) {
            offscreenRenderer.initialize()
            offscreenRendererInitialized = true
        }
        // Update the OpenGL renderer for the specified frame
        offscreenRenderer.setRotation(rotationY = frame.toFloat() * 10F)
//        imageView.setImageBitmap(
//            offscreenRenderer.renderOffscreen(
//                width = motionConfig.aspectRatio.width,
//                height = motionConfig.aspectRatio.height
//            )
//        )
        effects.forEach { it.forFrame(frame) }
        return this
    }

    override fun getViewBitmap(): Bitmap = offscreenRenderer.generateBitmap()!!

    override val assets: List<MotionAsset>
        get() = listOf(asset)
}
