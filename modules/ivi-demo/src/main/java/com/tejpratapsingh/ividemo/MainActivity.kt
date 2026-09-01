package com.tejpratapsingh.ividemo

import android.os.Bundle
import com.tejpratapsingh.ividemo.extension.enableSwipeSeekReverse
import com.tejpratapsingh.ividemo.motion.RenaultCar
import com.tejpratapsingh.motionlib.activities.PreviewActivity
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer

class MainActivity : PreviewActivity() {
    val video by lazy {
        MotionVideoProducer
            .with(
                context = applicationContext,
                motionConfig = motionConfig,
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        motionVideoPlayer.seekBar.min = 1

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
