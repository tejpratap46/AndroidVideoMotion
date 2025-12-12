package com.tejpratapsingh.motion.metadataextractor

import android.os.Parcelable
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.parcelize.Parcelize
import org.jsoup.Jsoup

@Parcelize
data class SocialMeta(
    val title: String? = null,
    val description: String? = null,
    val image: String? = null,
    val siteName: String? = null,
    val twitterCard: String? = null,
    val url: String? = null,
) : Parcelable

suspend fun HttpClient.extractSocialMetadata(url: String): SocialMeta? {
    try {
        val html = get(url).bodyAsText()
        val doc = Jsoup.parse(html)

        fun metaContent(vararg keys: String): String? {
            for (key in keys) {
                val el = doc.selectFirst("meta[property=$key], meta[name=$key]")
                if (el != null) return el.attr("content")
            }
            return null
        }

        return SocialMeta(
            title = metaContent("og:title") ?: doc.title(),
            description = metaContent("og:description", "description"),
            image = metaContent("og:image", "twitter:image"),
            siteName = metaContent("og:site_name"),
            twitterCard = metaContent("twitter:card"),
            url = metaContent("og:url", "twitter:url"),
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
