package com.tejpratapsingh.motionlib.pytorch.infra

import com.tejpratapsingh.motion.sdui.infra.MotionSdui
import com.tejpratapsingh.motionlib.pytorch.plugins.PyTorchBackgroundRemoverPlugin
import com.tejpratapsingh.motionlib.pytorch.plugins.PyTorchSuperResolutionPlugin

/**
 * Initializer for PyTorch based SDUI components.
 */
object PyTorchMotionSduiInitializer {
    fun initialize() {
        // Register PyTorchBackgroundRemoverPlugin
        MotionSdui.registerPlugin(PyTorchBackgroundRemoverPlugin::class.java.simpleName) { _, _ ->
            PyTorchBackgroundRemoverPlugin()
        }
        MotionSdui.registerPluginSerializer(PyTorchBackgroundRemoverPlugin::class.java) { _, _ -> }

        // Register PyTorchSuperResolutionPlugin
        MotionSdui.registerPlugin(PyTorchSuperResolutionPlugin::class.java.simpleName) { _, _ ->
            PyTorchSuperResolutionPlugin()
        }
        MotionSdui.registerPluginSerializer(PyTorchSuperResolutionPlugin::class.java) { _, _ -> }
    }
}
