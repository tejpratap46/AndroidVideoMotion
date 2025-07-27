package com.tejpratapsingh.motionlib.core.motion

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.core.adapter.AndroidVideoProducerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

open class MotionVideoProducer private constructor(
    val context: Context,
    val motionConfig: MotionConfig,
    val videoProducerAdapter: VideoProducerAdapter,
    val motionComposerView: MotionComposerView
) : IMotionVideoProducer {
    private val addedMotionViews = mutableListOf<MotionView>()

    var totalFrames: Int = 0
        private set

    companion object {
        private const val TAG = "MotionVideo"

        fun with(
            context: Context,
            config: MotionConfig,
            videoProducerAdapter: VideoProducerAdapter = AndroidVideoProducerAdapter()
        ) = MotionVideoProducer(
            context = context,
            motionConfig = config,
            videoProducerAdapter = videoProducerAdapter,
            motionComposerView = MotionComposerView(
                context = context, motionConfig = config
            )
        )
    }

    override fun addMotionViewToSequence(motionView: MotionView): MotionVideoProducer {
        if (motionView.endFrame < totalFrames) {
            throw IllegalStateException("add to sequence only accepts motion views with end frame")
        }
        totalFrames = maxOf(totalFrames, motionView.endFrame)
        recursiveSetMotionConfig(motionView)
        motionComposerView.apply {
            motionView.layoutBy(x = centerHorizontallyTo {
                parent.centerX()
            }, y = centerVerticallyTo {
                parent.centerY()
            })
        }
        addedMotionViews.add(motionView)
        return this
    }

    private fun recursiveSetMotionConfig(motionView: MotionView) {
        for (viewIndex in 0 until motionView.childCount) { // Use 'until'
            val view: View? = motionView.getChildAt(viewIndex)
            if (view != null && view is MotionView) {
                recursiveSetMotionConfig(motionView = view)
            }
        }
        motionView.motionConfig = this.motionConfig // Use instance motionConfig
    }

    override suspend fun produceVideo(
        outputFile: File, progressListener: ((progress: Int, bitmap: Bitmap) -> Unit)?
    ): File = withContext(Dispatchers.IO) { // Use Dispatchers.Default for CPU-bound work
        if (outputFile.exists()) {
            outputFile.delete()
        }

        videoProducerAdapter.produceVideo(motionConfig, motionComposerView, totalFrames, outputFile, progressListener)

        outputFile
    }
}