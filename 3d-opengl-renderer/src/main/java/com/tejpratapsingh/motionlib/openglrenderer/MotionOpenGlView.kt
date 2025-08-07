package com.tejpratapsingh.motionlib.openglrenderer

import android.content.Context
import android.graphics.Bitmap
import android.widget.FrameLayout
import android.widget.ImageView
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView

class MotionOpenGlView(
    context: Context,
    modelAssetPath: String,
    override val startFrame: Int,
    override val endFrame: Int,
) : FrameLayout(context), MotionView {

    override lateinit var motionConfig: MotionConfig

    private val imageView = ImageView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

//    private val offscreenRenderer: OffscreenRenderer

    val offscreenRenderer = Object3DToBitmapRenderer(
        context = context,
        assetFileName = modelAssetPath,
        width = if (::motionConfig.isInitialized) {
            motionConfig.width
        } else 500,
        height = if (::motionConfig.isInitialized) {
            motionConfig.height
        } else 500,
        objectColor = floatArrayOf(0.7f, 0.3f, 0.3f, 1.0f)
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
//                width = motionConfig.width,
//                height = motionConfig.height
//            )
//        )
        return this
    }

    override fun getViewBitmap(): Bitmap = offscreenRenderer.generateBitmap()!!
}