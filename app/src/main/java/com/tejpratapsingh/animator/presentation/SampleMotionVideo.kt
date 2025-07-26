package com.tejpratapsingh.animator.presentation

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.animator.ui.view.ContourDevice
import com.tejpratapsingh.motionlib.core.MotionVideoProducer
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.ui.custom.background.GradientView
import com.tejpratapsingh.motionlib.ui.custom.background.Orientation
import com.tejpratapsingh.motionlib.utils.MotionConfig

fun sampleMotionVideo(applicationContext: Context): MotionVideoProducer {
    val motionConfig = MotionConfig(
        width = 768,
        height = 1366,
        fps = 30
    )

    val motionView: MotionView = ContourDevice(
        context = applicationContext,
        startFrame = 1,
        endFrame = motionConfig.fps * 3
    )

    val motionView2: MotionView = GradientView(
        context = applicationContext,
        startFrame = motionConfig.fps * 3 + 1,
        endFrame = motionConfig.fps * 4,
        orientation = Orientation.CIRCULAR,
        intArrayOf(
            "#2568ff".toColorInt(),
            "#7048ff".toColorInt(),
            "#ba28ff".toColorInt()
        )
    ).apply {
        setBackgroundColor(Color.WHITE)
    }

    return MotionVideoProducer.with(applicationContext, motionConfig)
        .addMotionViewToSequence(motionView)
        .addMotionViewToSequence(motionView2)
}