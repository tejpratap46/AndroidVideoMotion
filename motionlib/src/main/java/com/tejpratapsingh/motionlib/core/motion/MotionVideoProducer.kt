package com.tejpratapsingh.motionlib.core.motion

import android.content.Context
import android.graphics.Bitmap
import android.view.View
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

open class MotionVideoProducer private constructor(
    val context: Context,
    val motionConfig: MotionConfig,
    val videoProducerAdapter: VideoProducerAdapter,
    val motionComposerView: MotionComposerView,
    val motionAudio: List<MotionAudio> = emptyList()
) : IMotionVideoProducer {
    var totalFrames: Int = 0
        private set

    companion object {
        private const val TAG = "MotionVideo"

        fun with(
            context: Context,
            config: MotionConfig,
            plugins: List<MotionPlugin> = emptyList(),
            videoProducerAdapter: VideoProducerAdapter = AndroidVideoProducerAdapter(),
            motionAudio: List<MotionAudio> = emptyList()
        ) = MotionVideoProducer(
            context = context,
            motionConfig = config,
            videoProducerAdapter = videoProducerAdapter,
            motionComposerView = MotionComposerView(
                context = context, motionConfig = config, plugins = plugins
            ),
            motionAudio = motionAudio
        )
    }

    override fun <T> addMotionViewToSequence(motionView: T): MotionVideoProducer where T : MotionView, T : ViewGroup {
        totalFrames = maxOf(totalFrames, motionView.endFrame)
        recursiveSetMotionConfig(motionView)
        motionComposerView.apply {
            motionView.layoutBy(x = centerHorizontallyTo {
                parent.centerX()
            }, y = centerVerticallyTo {
                parent.centerY()
            })
        }
        return this
    }

    private fun <T> recursiveSetMotionConfig(motionView: T) where T : MotionView, T : ViewGroup {
        motionView.motionConfig = this.motionConfig // Use instance motionConfig
        for (viewIndex in 0 until motionView.childCount) { // Use 'until'
            val view: View? = motionView.getChildAt(viewIndex)
            if (view != null && view is BaseMotionView) {
                recursiveSetMotionConfig(motionView = view)
            }
        }
    }

    override suspend fun produceVideo(
        context: Context,
        outputFile: File,
        progressListener: ((progress: Int, bitmap: Bitmap) -> Unit)?
    ): File = withContext(Dispatchers.IO) { // Use Dispatchers.Default for CPU-bound work
        if (outputFile.exists()) {
            outputFile.delete()
        }

        videoProducerAdapter.produceVideo(
            context = context,
            motionConfig = motionConfig,
            motionComposerView = motionComposerView,
            motionAudios = motionAudio,
            totalFrames = totalFrames,
            outputFile = outputFile,
            progressListener = progressListener
        )

        outputFile
    }
}