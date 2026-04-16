package com.tejpratapsingh.motionlib.jcodec

import android.content.Context
import android.graphics.Bitmap
import timber.log.Timber
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.core.extensions.compressToBitmap
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import org.jcodec.api.android.AndroidSequenceEncoder
import java.io.File

class JCodecVideoProducerAdapter : VideoProducerAdapter {
    override suspend fun produceVideo(
        context: Context,
        motionComposerViews: List<MotionView>,
        motionAudio: List<MotionAudio>,
        totalFrames: Int,
        outputFile: File,
        progressListener: (suspend (Int, Bitmap) -> Unit)?,
    ): File {
        Timber.i("produceVideo: starting production with JCodec (totalFrames: $totalFrames)")
        if (outputFile.exists()) {
            outputFile.delete()
        }
        val motionComposerView = motionComposerViews.firstOrNull()
            ?: error("At least one MotionView is required")
        val motionConfig: MotionConfig = provideCurrentConfig()
        Timber.d("produceVideo: Creating sequence encoder (fps: ${motionConfig.fps})")
        val encoder = AndroidSequenceEncoder.createSequenceEncoder(outputFile, motionConfig.fps)
        try {
            for (i in 1..totalFrames) {
                if (i % 10 == 0) {
                    Timber.v("produceVideo: frame $i/$totalFrames")
                }
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
            Timber.i("produceVideo: JCodec production complete")
        } catch (e: Exception) {
            Timber.e(e, "Error during JCodec production")
            throw e
        } finally {
            encoder.finish()
        }

        return outputFile
    }
}
