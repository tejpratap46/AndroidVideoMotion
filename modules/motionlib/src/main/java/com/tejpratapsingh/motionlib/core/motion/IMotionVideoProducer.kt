package com.tejpratapsingh.motionlib.core.motion

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import com.tejpratapsingh.motionlib.core.MotionView
import java.io.File

interface IMotionVideoProducer {
    fun <T> addMotionViewToSequence(motionView: T): MotionVideoProducer where T : MotionView, T : ViewGroup

    suspend fun produceVideo(
        context: Context,
        outputFile: File,
        progressListener: (suspend (progress: Int, bitmap: Bitmap) -> Unit)? = null,
    ): File
}
