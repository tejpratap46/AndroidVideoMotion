package com.tejpratapsingh.motionlib.core

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import com.tejpratapsingh.motionlib.extensions.compressToBitmap
import com.tejpratapsingh.motionlib.extensions.getViewBitmap
import com.tejpratapsingh.motionlib.utils.MotionConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jcodec.api.android.AndroidSequenceEncoder
import java.io.File

open class MotionVideoProducer private constructor(
    val context: Context, val motionConfig: MotionConfig, var motionComposerView: MotionComposerView
) : IMotionVideoProducer {
    private val addedMotionViews = mutableListOf<MotionView>()

    var totalFrames: Int = 0
        private set

    companion object {
        private const val TAG = "MotionVideo"

        fun with(
            context: Context, config: MotionConfig
        ) = MotionVideoProducer(
            context = context, motionConfig = config, motionComposerView = MotionComposerView(
                context = context, motionConfig = config // Pass the specific config
            )
        )
    }

    override fun addMotionViewToSequence(motionView: MotionView): MotionVideoProducer {
        if (motionView.endFrame < totalFrames) {
            throw IllegalStateException("add to sequence only accepts motion views with end frame")
        }
        totalFrames = maxOf(totalFrames, motionView.endFrame)
        recursiveSetMotionConfig(motionView)
        motionComposerView = motionComposerView.apply {
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
                recursiveSetMotionConfig(view)
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

        val encoder = AndroidSequenceEncoder.createSequenceEncoder(outputFile, motionConfig.fps)
        try {
            for (i in 1..totalFrames) {
                Log.d(TAG, "produceVideo: frame $i")
                val frameBitmap: Bitmap =
                    motionComposerView.forFrame(i).getViewBitmap().compressToBitmap(100)

                encoder.encodeImage(frameBitmap)

                progressListener?.let {
                    it(i, frameBitmap)
                }

                frameBitmap.recycle() // Be cautious with this, only if necessary.
            }
        } finally {
            encoder.finish()
        }

        outputFile
    }
}