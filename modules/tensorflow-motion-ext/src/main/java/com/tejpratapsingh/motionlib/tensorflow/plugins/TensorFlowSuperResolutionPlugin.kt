package com.tejpratapsingh.motionlib.tensorflow.plugins

import android.graphics.Bitmap
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.tensorflow.TensorFlowImageProcessor

class TensorFlowSuperResolutionPlugin : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap = TensorFlowImageProcessor.superResolutionPlugin.apply(input)
}
