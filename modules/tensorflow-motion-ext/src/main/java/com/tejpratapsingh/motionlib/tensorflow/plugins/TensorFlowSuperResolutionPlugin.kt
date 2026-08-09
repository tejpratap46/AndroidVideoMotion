package com.tejpratapsingh.motionlib.tensorflow.plugins

import android.graphics.Bitmap
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.tensorflow.TensorFlowImageProcessor
import org.koin.java.KoinJavaComponent.inject

class TensorFlowSuperResolutionPlugin : MotionPlugin {
    private val processor: TensorFlowImageProcessor by inject(TensorFlowImageProcessor::class.java)

    override fun apply(input: Bitmap): Bitmap = processor.superResolutionPlugin.apply(input)
}
