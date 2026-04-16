package com.tejpratapsingh.motion.metadataextractor.data

import timber.log.Timber
import com.tejpratapsingh.motionlib.core.extensions.DownloadException
import com.tejpratapsingh.motionlib.core.extensions.fetchBitmap
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.jsoup.Jsoup

class MetaDataFetcher {

    private val client = HttpClient(CIO)

    suspend fun extractSocialMetadata(url: String): MetaDataResult {
        Timber.d("extractSocialMetadata: $url")
        try {
            val html = client.get(url).bodyAsText()
            val doc = Jsoup.parse(html)

            Timber.d("extractSocialMetadata: downloaded")

            fun metaContent(vararg keys: String): String? {
                for (key in keys) {
                    val el = doc.selectFirst("meta[property=$key], meta[name=$key]")
                    if (el != null) return el.attr("content")
                }
                return null
            }

            return MetaDataResult.Success(
                SocialMeta(
                    title = metaContent("og:title") ?: doc.title(),
                    description = metaContent("og:description", "description"),
                    image = metaContent("og:image", "twitter:image"),
                    siteName = metaContent("og:site_name"),
                    twitterCard = metaContent("twitter:card"),
                    url = metaContent("og:url", "twitter:url"),
                ),
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract social metadata for url: $url")
            return MetaDataResult.Error(
                DownloadException("Failed to fetch metadata"),
            )
        }
    }

    suspend fun downloadImage(url: String) = client.fetchBitmap(url)

    fun close() {
        client.close()
    }
}
