package com.tejpratapsingh.animator.presentation

import RenaultCar
import android.content.Context
import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.animator.ui.view.ContourDevice
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.extensions.downloadFile
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.core.setCurrentConfig
import com.tejpratapsingh.motionlib.ui.custom.background.GradientView
import com.tejpratapsingh.motionlib.ui.custom.background.Orientation
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking
import java.io.File

fun sampleMotionVideo(applicationContext: Context): MotionVideoProducer {
    val motionConfig =
        MotionConfig(
            aspectRatio = VideoAspectRatio.Ratio9x16_480,
            fps = 30,
        )

    setCurrentConfig(motionConfig)

//    val assetManager = applicationContext.assets
//    val files = assetManager.list(RenaultCar.imageAssetSubFolder)
//
//    val motionView: BaseContourMotionView =
//        RenaultCar(
//            context = applicationContext,
//            startFrame = 1,
//            endFrame = files?.size ?: 1,
//        )

    val motionView =
        ContourDevice(
            context = applicationContext,
            startFrame = 1,
            endFrame = motionConfig.fps * 4,
        )

    val file = File(applicationContext.cacheDir, "arijit.m4a")

    if (!file.exists()) {
        val httpClient = HttpClient(CIO)
        runBlocking {
            try {
                httpClient.downloadFile(
                    file = file,
                    url =
                        "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/" +
                            "3d/be/de/3dbedeeb-4ef4-0b43-d23e-ed7b3ec0c034/mzaf_3312428321786187211.plus.aac.p.m4a",
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val motionAudio =
        listOf(
            MotionAudio(
                file = file,
                delayFrame = motionView.startFrame,
                startFrame = motionView.startFrame,
                endFrame = motionView.endFrame,
            ),
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

    val motionView2: MotionView =
        GradientView(
            context = applicationContext,
            startFrame = motionView.endFrame + 1,
            endFrame = motionConfig.fps * 4,
            orientation = Orientation.CIRCULAR,
            intArrayOf(
                "#2568ff".toColorInt(),
                "#7048ff".toColorInt(),
                "#ba28ff".toColorInt(),
            ),
        ).apply {
            setBackgroundColor(Color.WHITE)
        }

    return MotionVideoProducer
        .with(
            context = applicationContext,
            motionAudio = motionAudio,
        ).addMotionViewToSequence(motionView = motionView)
}
