package com.tejpratapsingh.motionlib.core

import android.graphics.Bitmap
import java.io.File

interface VideoProducerAdapter {
    suspend fun produceVideo(
        motionConfig: MotionConfig,
        motionComposerView: IMotionView,
        totalFrames:Int,
        outputFile: File,
        progressListener: ((Int, Bitmap) -> Unit)?
    ): File
}