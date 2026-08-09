package com.tejpratapsingh.motionlib.pytorch.plugins

import android.graphics.Bitmap
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.pytorch.PyTorchImageProcessor
import org.koin.java.KoinJavaComponent.inject

class PyTorchSuperResolutionPlugin : MotionPlugin {
    private val processor: PyTorchImageProcessor by inject(PyTorchImageProcessor::class.java)

    override fun apply(input: Bitmap): Bitmap = processor.superResolutionPlugin.apply(input)
}
