package com.tejpratapsingh.motionlib.core

import android.content.Context
import android.graphics.Bitmap
import java.io.File

interface VideoProducerAdapter {
    suspend fun produceVideo(
        context: Context,
        motionConfig: MotionConfig,
        motionComposerView: MotionView,
        motionAudio: List<MotionAudio>,
        totalFrames:Int,
        outputFile: File,
        progressListener: ((Int, Bitmap) -> Unit)?
    ): File
}