package com.tejpratapsingh.animator.presentation

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.animator.ui.view.ContourDevice
import com.tejpratapsingh.motionlib.core.MotionVideo
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.templates.views.DeviceFrameView
import com.tejpratapsingh.motionlib.ui.custom.background.GradientView
import com.tejpratapsingh.motionlib.ui.custom.background.Orientation
import com.tejpratapsingh.motionlib.utils.MotionConfig
import java.io.File
import java.io.FileInputStream

fun sampleMotionVideo(applicationContext: Context): MotionVideo {
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

    val motionView3: MotionView = object : MotionView(applicationContext, motionConfig.fps * 4 + 1, motionConfig.fps * 6) {
        val imageFile = File.createTempFile("testImage", "jpg")

        val deviceFrameView: DeviceFrameView = DeviceFrameView(context = context, bitmap = BitmapFactory.decodeStream(FileInputStream(imageFile)))

        override fun forFrame(frame: Int): View {
            super.forFrame(frame)

            deviceFrameView.layoutBy(
                x = leftTo {
                    parent.left()
                }.rightTo {
                    parent.right()
                },
                y = topTo {
                    parent.top()
                }.bottomTo {
                    parent.bottom()
                }
            )

            contourHeightOf {
                motionConfig.height.toYInt()
            }
            contourWidthOf {
                motionConfig.width.toXInt()
            }

            return this
        }
    }

    return MotionVideo.with(applicationContext, motionConfig)
        .addMotionViewToSequence(motionView)
        .addMotionViewToSequence(motionView2)
        .addMotionViewToSequence(motionView3)
}