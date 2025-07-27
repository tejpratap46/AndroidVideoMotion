package com.tejpratapsingh.motionlib.core.adapter

import android.graphics.Bitmap
import android.util.Log
import com.tejpratapsingh.motionlib.core.IMotionView
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.core.motion.AndroidVideoGenerator
import com.tejpratapsingh.motionlib.core.extensions.compressToBitmap
import com.tejpratapsingh.motionlib.core.extensions.getViewBitmap
import java.io.File

class AndroidVideoProducerAdapter: VideoProducerAdapter {

    companion object {
        private const val TAG = "AndroidVideoProducerAda"
    }

    val androidVideoGenerator = AndroidVideoGenerator()

    override suspend fun produceVideo(
        motionConfig: MotionConfig,
        motionComposerView: IMotionView,
        totalFrames:Int,
        outputFile: File,
        progressListener: ((Int, Bitmap) -> Unit)?
    ): File {
        if (outputFile.exists()) {
            outputFile.delete()
        }
        val mutableList: MutableList<Bitmap> = mutableListOf()
        for (i in 1..totalFrames) {
            Log.d(TAG, "produceVideo: frame $i")
            val frameBitmap: Bitmap =
                motionComposerView.forFrame(i).getViewBitmap()
                    .compressToBitmap(motionConfig.outputQuality)

            mutableList.add(frameBitmap)

            progressListener?.let {
                it(i, frameBitmap)
            }
        }
        androidVideoGenerator.generateVideo(
            bitmaps = mutableList,
            outputFile = outputFile,
            motionConfig = motionConfig
        )

        mutableList.forEach { it.recycle() }

        return outputFile
    }
}