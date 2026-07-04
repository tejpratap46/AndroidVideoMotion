package com.tejpratapsingh.motionlib.pytorch.plugins

import android.graphics.Bitmap
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.pytorch.PyTorchImageProcessor

class PyTorchSuperResolutionPlugin : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap = PyTorchImageProcessor.superResolutionPlugin.apply(input)
}
