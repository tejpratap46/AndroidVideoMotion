package com.tejpratapsingh.motionlib.tensorflow

import android.content.Context
import android.graphics.Bitmap
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.tensorflow.removebg.CarBgRemover
import com.tejpratapsingh.motionlib.tensorflow.superres.SuperResolutionProcessor

object TensorFlowImageProcessor {
    private lateinit var backgroundRemover: CarBgRemover
    private lateinit var superResolutionProcessor: SuperResolutionProcessor

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

    /**
     * Initializes the TensorFlow image processor with the given context.
     * This method should be called before using any of the plugins.
     *
     * @param context The application context.
     */
    @JvmStatic
    @Synchronized
    fun init(context: Context) {
        backgroundRemover = CarBgRemover(context)
        superResolutionProcessor = SuperResolutionProcessor(context, "ESRGAN_gh.tflite")
    }
}
