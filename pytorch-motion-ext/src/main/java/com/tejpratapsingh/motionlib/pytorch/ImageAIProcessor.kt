package com.tejpratapsingh.motionlib.pytorch

import android.content.Context
import android.graphics.Bitmap
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.pytorch.removebg.RemoveBg
import com.tejpratapsingh.motionlib.pytorch.superres.ImageUpscaler

object ImageAIProcessor {
    private lateinit var backgroundRemover: RemoveBg
    private lateinit var superResolutionProcessor: ImageUpscaler

    val backgroundRemoverPlugin: MotionPlugin by lazy {
        object : MotionPlugin {
            override fun apply(input: Bitmap): Bitmap {
                return backgroundRemover.clearBackground(input)
                    ?: throw IllegalStateException("Super Resolution processing failed")
            }
        }
    }

    val superResolutionPlugin: MotionPlugin by lazy {
        object : MotionPlugin {
            override fun apply(input: Bitmap): Bitmap {
                return superResolutionProcessor.upscaleImage(input)
                    ?: throw IllegalStateException("Super Resolution processing failed")
            }
        }
    }

    fun init(context: Context) {
        backgroundRemover = RemoveBg(context)
        superResolutionProcessor = ImageUpscaler(context)
    }
}
