package com.tejpratapsingh.animator.presentation

import RenaultCar
import android.content.Context
import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.core.motion.MotionView
import com.tejpratapsingh.motionlib.ffmpeg.FfmpegVideoProducerAdapter
import com.tejpratapsingh.motionlib.ui.custom.background.GradientView
import com.tejpratapsingh.motionlib.ui.custom.background.Orientation

fun sampleMotionVideo(applicationContext: Context): MotionVideoProducer {
    val motionConfig = MotionConfig(
        width = 768,
        height = 1366,
        fps = 30
    )

    val assetManager = applicationContext.assets
    val files = assetManager.list(RenaultCar.imageAssetSubFolder)

    val motionView: MotionView = RenaultCar(
        context = applicationContext,
        startFrame = 1,
        endFrame = files?.size ?: 1
    )

    val motionView2: MotionView = GradientView(
        context = applicationContext,
        startFrame = motionView.endFrame + 1,
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

    return MotionVideoProducer.with(
        context = applicationContext,
        config = motionConfig,
        videoProducerAdapter = FfmpegVideoProducerAdapter().apply { context = applicationContext })
        .addMotionViewToSequence(motionView = motionView)
        .addMotionViewToSequence(motionView = motionView2)
}