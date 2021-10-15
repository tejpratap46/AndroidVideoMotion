package com.tejpratapsingh.motionlib.ui

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.utils.MotionConfig
import com.tejpratapsingh.motionlib.utils.Utilities
import org.jcodec.api.android.AndroidSequenceEncoder
import java.io.File

open class MotionComposerView(context: Context, val motionConfig: MotionConfig) :
    ContourLayout(context), OnMotionFrameListener {
    private val TAG = "MotionComposerView"

    init {
        this.layout(0, 0, motionConfig.width, motionConfig.height)
    }

    fun produceVideo(outputFile: File): File {
        if (outputFile.exists()) {
            outputFile.delete()
        }

        val encoder = AndroidSequenceEncoder.createSequenceEncoder(outputFile, motionConfig.fps)

        for (i in 1..motionConfig.totalFrames) {
            forFrame(i)
            Log.d(TAG, "produceVideo: frame $i")
            val frameBitmap: Bitmap = Utilities.getViewBitmap(this)

            encoder.encodeImage(frameBitmap)
        }

        encoder.finish()

        return outputFile
    }

    override fun forFrame(frame: Int) {
        for (i in 0..this.childCount) {
            val view = this.getChildAt(i)

            if (view is MotionView) {
                view.forFrame(frame)
            }
        }
    }
}