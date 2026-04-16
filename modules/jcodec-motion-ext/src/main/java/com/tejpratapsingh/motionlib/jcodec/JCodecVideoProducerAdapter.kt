package com.tejpratapsingh.motionlib.jcodec

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.core.extensions.compressToBitmap
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import org.jcodec.api.android.AndroidSequenceEncoder
import java.io.File

class JCodecVideoProducerAdapter : VideoProducerAdapter {
    companion object {
        private const val TAG = "JCodecVideoProducerAdap"
    }

    override suspend fun produceVideo(
        context: Context,
        motionComposerView: MotionView,
        motionAudio: List<MotionAudio>,
        totalFrames: Int,
        outputFile: File,
        progressListener: (suspend (Int, Bitmap) -> Unit)?,
    ): File {
        if (outputFile.exists()) {
            outputFile.delete()
        }
        val motionConfig: MotionConfig = provideCurrentConfig()
        val encoder = AndroidSequenceEncoder.createSequenceEncoder(outputFile, motionConfig.fps)
        try {
            for (i in 1..totalFrames) {
                Log.d(TAG, "produceVideo: frame $i")
                val frameBitmap: Bitmap =
                    motionComposerView
                        .forFrame(i)
                        .getViewBitmap()
                        .compressToBitmap(motionConfig.outputQuality)

                encoder.encodeImage(frameBitmap)

                progressListener?.let {
                    it(i, frameBitmap)
                }

                frameBitmap.recycle() // Be cautious with this, only if necessary.
            }
        } finally {
            encoder.finish()
        }

        return outputFile
    }
}
