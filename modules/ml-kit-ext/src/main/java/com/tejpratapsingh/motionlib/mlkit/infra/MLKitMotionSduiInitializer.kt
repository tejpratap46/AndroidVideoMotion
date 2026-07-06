package com.tejpratapsingh.motionlib.mlkit.infra

import com.tejpratapsingh.motion.sdui.infra.MotionSdui
import com.tejpratapsingh.motion.sdui.infra.parseMotionEffectProps
import com.tejpratapsingh.motionlib.mlkit.effects.SubjectSegmentationEffect
import com.tejpratapsingh.motionlib.mlkit.plugins.SubjectSegmentationPlugin

/**
 * Initializer for ML Kit based SDUI components.
 */
object MLKitMotionSduiInitializer {
    fun initialize() {
        // Register SubjectSegmentationPlugin
        MotionSdui.registerPlugin(SubjectSegmentationPlugin::class.java.simpleName) { _, _ ->
            SubjectSegmentationPlugin()
        }
        MotionSdui.registerPluginSerializer(SubjectSegmentationPlugin::class.java) { _, _ -> }

        // Register SubjectSegmentationEffect
        MotionSdui.registerEffect(SubjectSegmentationEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            SubjectSegmentationEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
            )
        }
        MotionSdui.registerEffectSerializer(SubjectSegmentationEffect::class.java) { _, _ -> }
    }
}
