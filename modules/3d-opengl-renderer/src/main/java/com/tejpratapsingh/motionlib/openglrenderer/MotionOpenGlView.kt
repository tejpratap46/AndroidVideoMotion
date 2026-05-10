package com.tejpratapsingh.motionlib.openglrenderer

import android.content.Context
import android.graphics.Bitmap
import android.widget.FrameLayout
import android.widget.ImageView
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.provideCurrentConfig

class MotionOpenGlView(
    context: Context,
    modelAssetPath: String,
    override val startFrame: Int,
    override val endFrame: Int,
    override val loop: Pair<Int, Int> = Pair(0, 0),
    effects: List<MotionEffect> = emptyList(),
) : FrameLayout(context),
    MotionView {
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

//    private val offscreenRenderer: OffscreenRenderer

    val offscreenRenderer =
        Object3DToBitmapRenderer(
            context = context,
            assetFileName = modelAssetPath,
            width = provideCurrentConfig().aspectRatio.width,
            height = provideCurrentConfig().aspectRatio.height,
            objectColor = floatArrayOf(0.7f, 0.3f, 0.3f, 1.0f),
        )

    init {
        // Initialize OpenGL renderer with the model asset path
        addView(imageView)
//        val model = ObjModel(context, modelAssetPath)
//        offscreenRenderer = OffscreenRenderer(model)
        offscreenRenderer.initialize()
    }

    override fun forFrame(frame: Int): MotionView {
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
}
