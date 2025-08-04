package com.tejpratapsingh.motionlib.openglrenderer

import android.content.Context
import android.graphics.Bitmap
import android.widget.FrameLayout
import android.widget.ImageView
import com.tejpratapsingh.motionlib.core.IMotionView
import com.tejpratapsingh.motionlib.core.MotionConfig

class MotionOpenGlView(
    context: Context,
    modelAssetPath: String,
    override val startFrame: Int,
    override val endFrame: Int,
) : FrameLayout(context), IMotionView {

    override lateinit var motionConfig: MotionConfig

    private val imageView = ImageView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    private val offscreenRenderer: OffscreenRenderer

    init {
        // Initialize OpenGL renderer with the model asset path
        addView(imageView)
        val model = ObjModel(context, modelAssetPath)
        offscreenRenderer = OffscreenRenderer(model)
    }

    override fun forFrame(frame: Int): IMotionView {
        // Update the OpenGL renderer for the specified frame
        offscreenRenderer.setRotation(frame.toFloat() * 10F)
        imageView.setImageBitmap(offscreenRenderer.renderOffscreen())
        return this
    }

    override fun getViewBitmap(): Bitmap = offscreenRenderer.renderOffscreen()
}