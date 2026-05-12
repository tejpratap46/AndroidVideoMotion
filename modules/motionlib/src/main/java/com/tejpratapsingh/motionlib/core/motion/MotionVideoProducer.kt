package com.tejpratapsingh.motionlib.core.motion

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.core.adapter.AndroidVideoProducerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

open class MotionVideoProducer private constructor(
    val context: Context,
    val videoProducerAdapter: VideoProducerAdapter,
    val motionComposerView: MotionComposerView,
    val motionAudio: List<MotionAudio> = emptyList(),
) : IMotionVideoProducer {
    var totalFrames: Int = 0
        private set

    companion object {
        @JvmStatic
        fun with(
            context: Context,
            plugins: List<MotionPlugin> = emptyList(),
            motionAudio: List<MotionAudio> = emptyList(),
            videoProducerAdapter: VideoProducerAdapter = AndroidVideoProducerAdapter(),
        ) = MotionVideoProducer(
            context = context,
            videoProducerAdapter = videoProducerAdapter,
            motionComposerView =
                MotionComposerView(
                    context = context,
                    plugins = plugins,
                ),
            motionAudio = motionAudio,
        )
    }

    override fun <T> addMotionViewToSequence(motionView: T): MotionVideoProducer where T : MotionView, T : ViewGroup {
        totalFrames = maxOf(totalFrames, motionView.endFrame)
        motionComposerView.apply {
            motionView.layoutBy(
                x =
                    centerHorizontallyTo {
                        parent.centerX()
                    },
                y =
                    centerVerticallyTo {
                        parent.centerY()
                    },
                addToViewGroup = true,
            )
        }
        return this
    }

    override suspend fun produceVideo(
        context: Context,
        outputFile: File,
        progressListener: (suspend (progress: Int, bitmap: Bitmap) -> Unit)?,
    ): File =
        withContext(Dispatchers.Default) {
            if (outputFile.exists()) {
                outputFile.delete()
            }

            videoProducerAdapter.produceVideo(
                context = context,
                motionComposerView = motionComposerView,
                motionAudio = motionAudio,
                totalFrames = totalFrames,
                outputFile = outputFile,
                progressListener = progressListener,
            )

            outputFile
        }
}
