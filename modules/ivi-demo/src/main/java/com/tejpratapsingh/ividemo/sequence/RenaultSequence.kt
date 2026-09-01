package com.tejpratapsingh.ividemo.sequence

import android.content.Context
import com.tejpratapsingh.ividemo.motion.RenaultCar
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer

fun sampleMotionVideo(applicationContext: Context): MotionVideoProducer {
    val motionConfig =
        MotionConfig(
            aspectRatio = VideoAspectRatio.Ratio9x16_480,
            fps = 30,
        )

    val assetManager = applicationContext.assets
    val files = assetManager.list(RenaultCar.IMAGE_ASSET_SUB_FOLDER)

    val motionView: BaseContourMotionView =
        RenaultCar(
            context = applicationContext,
            startFrame = 1,
            endFrame = files?.size ?: 1,
        )

    val motionView2: BaseContourMotionView =
        RenaultCar(
            context = applicationContext,
            startFrame = 1,
            endFrame = 55000,
        )

    return MotionVideoProducer
        .with(
            context = applicationContext,
            motionConfig = motionConfig,
        ).addMotionViewToSequence(motionView = motionView)
        .addMotionViewToSequence(motionView = motionView2)
}
