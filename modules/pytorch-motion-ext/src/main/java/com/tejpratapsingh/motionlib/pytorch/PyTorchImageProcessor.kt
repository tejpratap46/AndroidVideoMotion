package com.tejpratapsingh.motionlib.pytorch

import android.content.Context
import android.graphics.Bitmap
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.pytorch.removebg.RemoveBg
import com.tejpratapsingh.motionlib.pytorch.superres.ImageUpscaler

object PyTorchImageProcessor {
    private lateinit var backgroundRemover: RemoveBg
    private lateinit var superResolutionProcessor: ImageUpscaler

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

    /**
     * Initializes the PyTorchImageProcessor with the given context.
     * This method should be called once, typically in the Application class.
     *
     * @param context The application context.
     */
    @JvmStatic
    @Synchronized
    fun init(context: Context) {
        backgroundRemover = RemoveBg(context)
        superResolutionProcessor = ImageUpscaler(context)
    }
}
