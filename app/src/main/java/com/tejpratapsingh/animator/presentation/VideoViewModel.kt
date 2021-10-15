package com.tejpratapsingh.animator.presentation

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jcodec.api.SequenceEncoder
import org.jcodec.api.android.AndroidSequenceEncoder
import org.jcodec.common.AndroidUtil
import org.jcodec.common.model.ColorSpace
import java.io.File

class VideoViewModel : ViewModel() {

    private val renderedFile: MutableLiveData<File> by lazy {
        MutableLiveData<File>()
    }

    fun getFile(): LiveData<File> {
        return renderedFile
    }

    private val renderProgress: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun getProgress(): LiveData<Int> {
        return renderProgress
    }

    fun renderVideo(outputFile: File, bitmap1: Bitmap, bitmap2: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {

            if (outputFile.exists()) {
                outputFile.delete()
            }

            val encoder = AndroidSequenceEncoder.createSequenceEncoder(outputFile, 60)

            for (i in 1..99) {
                when {
                    i < 33 -> {
                        encoder.encodeImage(
                            bitmap1
                        )
                    }
                    i < 66 -> {
                        encoder.encodeImage(
                            bitmap2
                        )
                    }
                    else -> {
                        encoder.encodeImage(
                            bitmap1
                        )
                    }
                }

                renderProgress.postValue(i)
            }
            encoder.finish()

            renderedFile.postValue(outputFile)
        }
    }
}