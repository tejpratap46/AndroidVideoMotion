package com.tejpratapsingh.motionlib.mlkit

import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.mlkit.plugins.SubjectSegmentationPlugin

/**
 * Entry point for ML Kit based image processing plugins.
 */
class MLKitImageProcessor {
    /**
     * Plugin for background removal using ML Kit Subject Segmentation.
     */
    val subjectSegmentationPlugin: MotionPlugin by lazy {
        SubjectSegmentationPlugin()
    }
}
