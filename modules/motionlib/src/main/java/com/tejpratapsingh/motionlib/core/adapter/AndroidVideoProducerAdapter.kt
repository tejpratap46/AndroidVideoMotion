package com.tejpratapsingh.motionlib.core.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.core.findConfig
import com.tejpratapsingh.motionlib.core.infra.AndroidVideoGenerator
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.File

class AndroidVideoProducerAdapter : VideoProducerAdapter {
    private val androidVideoGenerator = AndroidVideoGenerator()

    override suspend fun produceVideo(
        context: Context,
        motionComposerView: MotionView,
        motionAudio: List<MotionAudio>,
        totalFrames: Int,
        outputFile: File,
        progressListener: (suspend (Int, Bitmap) -> Unit)?,
    ): File {
        Timber.i("produceVideo: starting direct surface rendering")
        if (outputFile.exists()) {
            outputFile.delete()
        }

        val motionConfig: MotionConfig = motionComposerView.findConfig()
        val composerAsView = motionComposerView as? View

        androidVideoGenerator.generateVideo(
            context = context,
            motionConfig = motionConfig,
            totalFrames = totalFrames,
            frameRenderer = { canvas, frameIndex ->
                val frameNum = frameIndex + 1
                val frameView = motionComposerView.forFrame(frameNum)
                val targetView = composerAsView ?: (frameView as? View)
                targetView?.let { view ->
                    view.measure(
                        View.MeasureSpec.makeMeasureSpec(motionConfig.aspectRatio.width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(motionConfig.aspectRatio.height, View.MeasureSpec.EXACTLY),
                    )
                    view.layout(0, 0, motionConfig.aspectRatio.width, motionConfig.aspectRatio.height)
                    view.draw(canvas)
                }

                progressListener?.let { listener ->
                    runBlocking {
                        val previewBmp = frameView.getViewBitmap()
                        listener(frameNum, previewBmp)
                        previewBmp.recycle()
                    }
                }
            },
            motionAudio = motionAudio,
            outputFile = outputFile,
        )

        return outputFile
    }
}
