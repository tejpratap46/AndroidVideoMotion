package com.tejpratapsingh.motion.metadataextractor.presentation

import android.graphics.Bitmap
import timber.log.Timber
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tejpratapsingh.motion.metadataextractor.data.MetaDataFetcher
import com.tejpratapsingh.motion.metadataextractor.data.MetaDataResult
import kotlinx.coroutines.launch

class MetaDataViewModel : ViewModel() {
    private val metaDataFetcher = MetaDataFetcher()
    private val _metadata = MutableLiveData<MetaDataResult>()
    val metadata: LiveData<MetaDataResult> = _metadata

    fun getMetaData(url: String) {
        viewModelScope.launch {
            Timber.i("getMetaData: url: $url")
            metaDataFetcher.extractSocialMetadata(url).let {
                Timber.i("getMetaData: result: $it")
                _metadata.postValue(it)
            }
        }
    }

    suspend fun downloadImage(url: String): Bitmap? = metaDataFetcher.downloadImage(url)

    override fun onCleared() {
        metaDataFetcher.close()
        super.onCleared()
    }
}
