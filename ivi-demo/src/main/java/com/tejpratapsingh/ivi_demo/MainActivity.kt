package com.tejpratapsingh.ivi_demo

import RenaultCar
import android.os.Bundle
import com.tejpratapsingh.ivi_demo.extension.enableSwipeSeekReverse
import com.tejpratapsingh.motionlib.activities.PreviewActivity
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.motion.BaseMotionView
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer

class MainActivity : PreviewActivity() {

    val video by lazy {
        MotionVideoProducer.with(
            context = applicationContext,
            config = motionConfig,
        ).addMotionViewToSequence(motionView = motionView)
    }

    val motionConfig = MotionConfig(
        width = 768, height = 1366, fps = 30
    )

    val motionView: BaseMotionView by lazy {
        RenaultCar(
            context = applicationContext,
            startFrame = 1,
            endFrame = 72
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        motionView.enableSwipeSeekReverse(
            maxProgress = video.totalFrames,
            initialProgress = { motionVideoPlayer.seekBar.progress },
            onProgressChanged = { newProgress ->
                motionVideoPlayer.seekBar.progress = newProgress
                video.motionComposerView.forFrame(newProgress)
            },
            sensitivity = 5f
        )
    }

    override fun getMotionVideo(): MotionVideoProducer {
        return video
    }
}