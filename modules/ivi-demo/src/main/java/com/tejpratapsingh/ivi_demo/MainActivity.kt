package com.tejpratapsingh.ivi_demo

import RenaultCar
import android.os.Build
import android.os.Bundle
import com.tejpratapsingh.ivi_demo.extension.enableSwipeSeekReverse
import com.tejpratapsingh.motionlib.activities.PreviewActivity
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.core.setCurrentConfig

class MainActivity : PreviewActivity() {
    val video by lazy {
        MotionVideoProducer
            .with(
                context = applicationContext,
            ).addMotionViewToSequence(motionView = motionView)
    }

    val motionConfig =
        MotionConfig(
            aspectRatio = VideoAspectRatio.Ratio16x9_480,
            fps = 30,
        )

    val motionView: BaseContourMotionView by lazy {
        RenaultCar(
            context = applicationContext,
            startFrame = 1,
            endFrame = 72,
        )
    }

    init {
        setCurrentConfig(motionConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            motionVideoPlayer.seekBar.min = 1
        }

        motionView.enableSwipeSeekReverse(
            maxProgress = video.totalFrames,
            initialProgress = { motionVideoPlayer.seekBar.progress },
            onProgressChanged = { newProgress ->
                motionVideoPlayer.seekBar.progress = newProgress
                video.motionComposerView.forFrame(newProgress)
            },
            sensitivity = 5f,
        )
    }

    override fun getMotionVideo(): MotionVideoProducer = video
}
