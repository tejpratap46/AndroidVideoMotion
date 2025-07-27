package com.tejpratapsingh.motionlib.core.motion

import android.graphics.Bitmap
import java.io.File

interface IMotionVideoProducer {
    fun addMotionViewToSequence(motionView: MotionView): MotionVideoProducer
    suspend fun produceVideo(
        outputFile: File, progressListener: ((progress: Int, bitmap: Bitmap) -> Unit)? = null
    ): File
}