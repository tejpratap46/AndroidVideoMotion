package com.tejpratapsingh.motionlib.tensorflow

import android.content.Context
import android.graphics.Bitmap
import com.tejpratapsingh.motionlib.core.MotionPlugin

object ImageAIProcessor {
    private lateinit var backgroundRemover: TiledBackgroundRemover
    private lateinit var superResolutionProcessor: SuperResolutionProcessor

    val superResolutionPlugin: MotionPlugin by lazy {
        object : MotionPlugin {
            override fun apply(input: Bitmap): Bitmap {
                return superResolutionProcessor.enhance(input)
            }
        }
    }

    fun init(context: Context) {
        backgroundRemover = TiledBackgroundRemover(context, "deeplabv3_257_mv_gpu.tflite")
        superResolutionProcessor = SuperResolutionProcessor(context, "esrgan_int8.tflite")
    }
}
