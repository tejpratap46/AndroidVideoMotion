package com.tejpratapsingh.motion.metadataextractor.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SocialMeta(
    val title: String? = null,
    val description: String? = null,
    val image: String? = null,
    val siteName: String? = null,
    val twitterCard: String? = null,
    val url: String? = null,
) : Parcelable

sealed class MetaDataResult {
    data class Success(
        val metaData: SocialMeta,
    ) : MetaDataResult()

    data class Error(
        val error: Exception,
    ) : MetaDataResult()
}
