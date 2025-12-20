package com.tejpratapsingh.motion.ongoing.domain

class CurrentProject(
    var title: String? = null,
    var description: String? = null,
    var image: String? = null,
    var siteName: String? = null,
    var twitterCard: String? = null,
    var url: String? = null,
    var trackName: String? = null,
    var artistName: String? = null,
    var albumName: String? = null,
    var duration: Float? = null,
    var instrumental: Boolean? = null,
    var plainLyrics: String? = null,
    var syncedLyrics: String? = null,
    var savedFilePath: String? = null,
    var totalLyrics: String? = null,
    var selectedLyrics: String? = null,
) {
    override fun toString(): String {
        return "title:$title, description:$description, image:$image, siteName:$siteName, twitterCard:$twitterCard, url:$url, trackName:$trackName," +
                "artistName:$artistName, albumName:$albumName, duration:$duration, instrument:$instrumental, plainLyrics:$plainLyrics, syncedLyrics:$syncedLyrics," +
                "savedFilePath:$savedFilePath, totalLyrics:$totalLyrics, selectedLyrics:$selectedLyrics"
    }
}