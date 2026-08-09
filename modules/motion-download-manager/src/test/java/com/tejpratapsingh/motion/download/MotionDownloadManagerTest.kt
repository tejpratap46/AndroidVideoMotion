package com.tejpratapsingh.motion.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MotionDownloadManagerTest {

    @Test
    fun testExtractUrls() {
        val sdui = """
            {
              "image": "https://example.com/image.jpg",
              "video": "http://test.com/video.mp4",
              "other": "some text https://another.com/file.png more text"
            }
        """.trimIndent()

        val urls = MotionDownloadManager.extractUrls(sdui)

        assertEquals(3, urls.size)
        assertTrue(urls.contains("https://example.com/image.jpg"))
        assertTrue(urls.contains("http://test.com/video.mp4"))
        assertTrue(urls.contains("https://another.com/file.png"))
    }

    @Test
    fun testReplaceUrls() {
        val sdui = """
            {
              "image": "https://example.com/image.jpg"
            }
        """.trimIndent()

        val urlToPath = mapOf("https://example.com/image.jpg" to "/data/user/0/com.test/files/image.jpg")
        val modified = MotionDownloadManager.replaceUrlsWithLocalPaths(sdui, urlToPath)

        assertTrue(modified.contains("file:///data/user/0/com.test/files/image.jpg"))
        assertTrue(!modified.contains("https://example.com/image.jpg"))
    }
}
