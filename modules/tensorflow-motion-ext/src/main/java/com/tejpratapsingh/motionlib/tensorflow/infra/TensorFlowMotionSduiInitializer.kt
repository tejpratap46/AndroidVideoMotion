package com.tejpratapsingh.motionlib.tensorflow.infra

import com.tejpratapsingh.motion.sdui.infra.MotionSdui
import com.tejpratapsingh.motionlib.tensorflow.plugins.TensorFlowBackgroundRemovalPlugin
import com.tejpratapsingh.motionlib.tensorflow.plugins.TensorFlowSuperResolutionPlugin

/**
 * Initializer for TensorFlow based SDUI components.
 */
object TensorFlowMotionSduiInitializer {
    fun initialize() {
        // Register TensorFlowBackgroundRemovalPlugin
        MotionSdui.registerPlugin(TensorFlowBackgroundRemovalPlugin::class.java.simpleName) { _, _ ->
            TensorFlowBackgroundRemovalPlugin()
        }
        MotionSdui.registerPluginSerializer(TensorFlowBackgroundRemovalPlugin::class.java) { _, _ -> }

        // Register TensorFlowSuperResolutionPlugin
        MotionSdui.registerPlugin(TensorFlowSuperResolutionPlugin::class.java.simpleName) { _, _ ->
            TensorFlowSuperResolutionPlugin()
        }
        MotionSdui.registerPluginSerializer(TensorFlowSuperResolutionPlugin::class.java) { _, _ -> }
    }
}
