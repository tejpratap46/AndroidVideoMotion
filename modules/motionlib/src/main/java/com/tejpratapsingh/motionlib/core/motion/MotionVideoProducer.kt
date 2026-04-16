package com.tejpratapsingh.motionlib.core.motion

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.core.adapter.AndroidVideoProducerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

open class MotionVideoProducer(
    val context: Context,
    val videoProducerAdapter: VideoProducerAdapter,
    val motionComposerView: MotionComposerView,
    val parallelMotionViews: List<MotionView> = emptyList(),
    val motionAudio: List<MotionAudio> = emptyList(),
) : IMotionVideoProducer {
    var totalFrames: Int = 0
        private set

    companion object {
        private const val TAG = "MotionVideo"

        @JvmStatic
        fun with(
            context: Context,
            plugins: List<MotionPlugin> = emptyList(),
            motionAudio: List<MotionAudio> = emptyList(),
            videoProducerAdapter: VideoProducerAdapter = AndroidVideoProducerAdapter(),
            parallelMotionViews: List<MotionView> = emptyList(),
        ) = MotionVideoProducer(
            context = context,
            videoProducerAdapter = videoProducerAdapter,
            motionComposerView =
                MotionComposerView(
                    context = context,
                    plugins = plugins,
                ),
            parallelMotionViews = parallelMotionViews,
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
            )
        }
        return this
    }

    override suspend fun produceVideo(
        context: Context,
        outputFile: File,
        progressListener: (suspend (progress: Int, bitmap: Bitmap) -> Unit)?,
    ): File =
        withContext(Dispatchers.IO) {
            // Use Dispatchers.Default for CPU-bound work
            if (outputFile.exists()) {
                outputFile.delete()
            }

            videoProducerAdapter.produceVideo(
                context = context,
                motionComposerViews =
                    if (parallelMotionViews.isEmpty()) {
                        listOf(motionComposerView)
                    } else {
                        parallelMotionViews
                    },
                motionAudio = motionAudio,
                totalFrames = totalFrames,
                outputFile = outputFile,
                progressListener = progressListener,
            )

            outputFile
        }
}
