package com.tejpratapsingh.motionlib.tensorflow

import android.content.Context
import android.graphics.Bitmap
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.tensorflow.removebg.CarBgRemover
import com.tejpratapsingh.motionlib.tensorflow.superres.SuperResolutionProcessor

class TensorFlowImageProcessor(context: Context) {
    private val backgroundRemover = CarBgRemover(context)
    private val superResolutionProcessor = SuperResolutionProcessor(context, "ESRGAN_gh.tflite")

    /**
     * Plugin for super-resolution using TensorFlow Lite.
     * This plugin uses a super-resolution processor to enhance the input bitmap.
     */
    val superResolutionPlugin: MotionPlugin by lazy {
        object : MotionPlugin {
            override fun apply(input: Bitmap): Bitmap = superResolutionProcessor.enhance(input)
        }
    }

    /**
     * Plugin for background removal using TensorFlow Lite.
     * This plugin uses a tiled background remover to process the input bitmap.
     */
    val backgroundRemovalPlugin: MotionPlugin by lazy {
        object : MotionPlugin {
            override fun apply(input: Bitmap): Bitmap = backgroundRemover.removeBackground(input)
        }
    }
}
