package com.tejpratapsingh.motionlib.pytorch.plugins

import android.graphics.Bitmap
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.pytorch.PyTorchImageProcessor

class PyTorchBackgroundRemoverPlugin : MotionPlugin {
    override fun apply(input: Bitmap): Bitmap = PyTorchImageProcessor.backgroundRemoverPlugin.apply(input)
}
