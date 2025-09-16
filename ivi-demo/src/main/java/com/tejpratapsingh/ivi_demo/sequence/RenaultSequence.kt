package com.tejpratapsingh.ivi_demo.sequence

import RenaultCar
import android.content.Context
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer

fun sampleMotionVideo(applicationContext: Context): MotionVideoProducer {
    val motionConfig = MotionConfig(
        aspectRatio = VideoAspectRatio.Ratio9x16_480, fps = 30
    )

    val assetManager = applicationContext.assets
    val files = assetManager.list(RenaultCar.imageAssetSubFolder)

    val motionView: BaseContourMotionView = RenaultCar(
        context = applicationContext,
        startFrame = 1,
        endFrame = files?.size ?: 1
    )

    return MotionVideoProducer.with(
        context = applicationContext,
        config = motionConfig,
    ).addMotionViewToSequence(motionView = motionView)
}