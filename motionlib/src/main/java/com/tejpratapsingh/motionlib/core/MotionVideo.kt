package com.tejpratapsingh.motionlib.core

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import com.tejpratapsingh.motionlib.extensions.compressToBitmap
import com.tejpratapsingh.motionlib.extensions.getViewBitmap
import com.tejpratapsingh.motionlib.ui.MotionComposerView
import com.tejpratapsingh.motionlib.utils.MotionConfig
import org.jcodec.api.android.AndroidSequenceEncoder
import java.io.File

open class MotionVideo private constructor(
    val context: Context,
    val motionConfig: MotionConfig
) {
    private val TAG = "MotionVideo"

    private val addedMotionViews = mutableListOf<MotionView>()

    lateinit var motionComposerView: MotionComposerView

    var totalFrames: Int = 0

    companion object {
        lateinit var motionConfig: MotionConfig

        fun with(context: Context, motionConfig: MotionConfig): MotionVideo {
            MotionVideo.motionConfig = motionConfig

            val instance = MotionVideo(context = context, motionConfig = motionConfig)
            instance.motionComposerView = MotionComposerView(
                context = context,
                motionConfig = motionConfig
            )
            return instance
        }
    }

    fun addMotionViewToSequence(motionView: MotionView): MotionVideo {
        if (motionView.endFrame < totalFrames) {
            throw IllegalStateException("add to sequence only accepts motion views with end frame")
        }
        totalFrames = maxOf(totalFrames, motionView.endFrame)
        recursiveSetMotionConfig(motionView)
        motionComposerView = motionComposerView.apply {
            motionView.layoutBy(
                x = centerHorizontallyTo {
                    parent.centerX()
                },
                y = centerVerticallyTo {
                    parent.centerY()
                }
            )
        }

        addedMotionViews.add(motionView)

        return this
    }

    private fun recursiveSetMotionConfig(motionView: MotionView) {
        for (viewIndex in 0..motionView.childCount) {
            val view: View? = motionView.getChildAt(viewIndex)
            if (view != null && view is MotionView) {
                Log.d(TAG, "recursiveSetMotionConfig: motionView.endFrame: ${motionView.endFrame}")
                Log.d(TAG, "recursiveSetMotionConfig: totalFrames: ${totalFrames}")
                if (motionView.endFrame < totalFrames) {
                    throw IllegalStateException("add to sequence only accepts motion views with end frame")
                }
                recursiveSetMotionConfig(view)
            }
        }
        motionView.motionConfig = motionConfig
    }

    fun produceVideo(
        outputFile: File,
        progressListener: ((progress: Int, bitmap: Bitmap) -> Unit)? = null
    ): File {
        if (outputFile.exists()) {
            outputFile.delete()
        }

        val encoder = AndroidSequenceEncoder.createSequenceEncoder(outputFile, motionConfig.fps)

        for (i in 1..totalFrames) {
            Log.d(TAG, "produceVideo: frame $i")
            val frameBitmap: Bitmap =
                motionComposerView.forFrame(i).getViewBitmap().compressToBitmap(100)

            encoder.encodeImage(frameBitmap)

            progressListener?.let {
                it(i, frameBitmap)
            }
        }

        encoder.finish()

        return outputFile
    }
}