package com.tejpratapsingh.motionlib.pytorch

import android.content.Context
import android.graphics.Bitmap
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.pytorch.removebg.RemoveBg
import com.tejpratapsingh.motionlib.pytorch.superres.ImageUpscaler

class PyTorchImageProcessor(context: Context) {
    private val backgroundRemover = RemoveBg(context)
    private val superResolutionProcessor = ImageUpscaler(context)

    /**
     * Plugin for background removal using PyTorch.
     * This plugin processes the input Bitmap to remove its background.
     */
    val backgroundRemoverPlugin: MotionPlugin by lazy {
        object : MotionPlugin {
            override fun apply(input: Bitmap): Bitmap =
                backgroundRemover.clearBackground(input)
                    ?: throw IllegalStateException("Super Resolution processing failed")
        }
    }

    /**
     * Plugin for super-resolution using PyTorch.
     * This plugin processes the input Bitmap to upscale it.
     */
    val superResolutionPlugin: MotionPlugin by lazy {
        object : MotionPlugin {
            override fun apply(input: Bitmap): Bitmap =
                superResolutionProcessor.upscaleImage(input)
                    ?: throw IllegalStateException("Super Resolution processing failed")
        }
    }
}
