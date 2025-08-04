package com.tejpratapsingh.motionlib.core.motion

import android.graphics.Bitmap
import android.view.ViewGroup
import com.tejpratapsingh.motionlib.core.IMotionView
import java.io.File

interface IMotionVideoProducer {
    fun <T> addMotionViewToSequence(motionView: T): MotionVideoProducer where T : IMotionView, T : ViewGroup
    suspend fun produceVideo(
        outputFile: File, progressListener: ((progress: Int, bitmap: Bitmap) -> Unit)? = null
    ): File
}