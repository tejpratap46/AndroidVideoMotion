package com.tejpratapsingh.animator.presentation

import RenaultCar
import android.content.Context
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.motion.BaseMotionView
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer

fun sampleMotionVideo(applicationContext: Context): MotionVideoProducer {
    val motionConfig = MotionConfig(
        width = 768, height = 1366, fps = 30
    )

    val assetManager = applicationContext.assets
    val files = assetManager.list(RenaultCar.imageAssetSubFolder)

    val motionView: BaseMotionView = RenaultCar(
        context = applicationContext,
        startFrame = 1,
        endFrame = files?.size ?: 1
    )

//    val motionView = MotionOpenGlView(
//        context = applicationContext,
//        modelAssetPath = "model/bug.obj",
//        startFrame = 1,
//        endFrame = 360
//    )

//    val motionView = VideoFrameView(
//        context = applicationContext,
//        videoUri = applicationContext.getUriFromAsset(
//            assetFilePath = "bg/172896-848187907_tiny.mp4"
//        ),
//        startFrame = 1,
//        endFrame = 360
//    )

//    val motionView = FFMpegVideoFrameView(
//        context = applicationContext,
//        videoFile = applicationContext.getFileFromAsset(
//            assetFilePath = "bg/172896-848187907_tiny.mp4"
//        ),
//        startFrame = 1,
//        endFrame = 360
//    )

//    val motionView = Filament3dView(
//        context = applicationContext,
//        modelAssetPath = "model/jeep.glb",
//        startFrame = 1,
//        endFrame = 360,
//        motionConfig = motionConfig
//    )

    /*val motionView2: MotionView = GradientView(
        context = applicationContext,
        startFrame = motionView.endFrame + 1,
        endFrame = motionConfig.fps * 4,
        orientation = Orientation.CIRCULAR,
        intArrayOf(
            "#2568ff".toColorInt(), "#7048ff".toColorInt(), "#ba28ff".toColorInt()
        )
    ).apply {
        setBackgroundColor(Color.WHITE)
    }*/

    return MotionVideoProducer.with(
        context = applicationContext,
        config = motionConfig,
    ).addMotionViewToSequence(motionView = motionView)
}