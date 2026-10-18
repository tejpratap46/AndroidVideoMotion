package com.tejpratapsingh.motion.assetstore

import com.google.gson.Gson
import com.tejpratapsingh.motion.assetstore.service.pexels.PexelsAssetService
import com.tejpratapsingh.motion.assetstore.service.pexels.PexelsConfig
import com.tejpratapsingh.motion.assetstore.service.pexels.PexelsPhotoResponse
import com.tejpratapsingh.motion.assetstore.service.pexels.PexelsVideoResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PexelsAssetServiceTest {
    @Test
    fun testPexelsServiceMetadata() {
        val client = HttpClient(CIO)
        val gson = Gson()
        val service = PexelsAssetService(client, gson, PexelsConfig())

        assertEquals("pexels", service.serviceId)
        assertEquals("Pexels", service.serviceName)
        assertEquals(2, service.supportedTypes.size)
    }

    @Test
    fun testPexelsPhotoJsonResponseParsing() {
        val json = """
            {
              "page": 1,
              "per_page": 15,
              "total_results": 100,
              "next_page": "https://api.pexels.com/v1/curated?page=2",
              "photos": [
                {
                  "id": 12345,
                  "width": 1920,
                  "height": 1080,
                  "url": "https://www.pexels.com/photo/12345/",
                  "photographer": "John Doe",
                  "photographer_url": "https://www.pexels.com/@johndoe",
                  "alt": "Sample Photo",
                  "src": {
                    "original": "https://images.pexels.com/photos/12345/original.jpg",
                    "medium": "https://images.pexels.com/photos/12345/medium.jpg"
                  }
                }
              ]
            }
        """.trimIndent()

        val gson = Gson()
        val response = gson.fromJson(json, PexelsPhotoResponse::class.java)

        assertEquals(1, response.page)
        assertEquals(15, response.perPage)
        assertEquals(100, response.totalResults)
        assertEquals(1, response.photos.size)

        val photo = response.photos.first()
        assertEquals(12345L, photo.id)
        assertEquals("John Doe", photo.photographer)
        assertEquals("https://images.pexels.com/photos/12345/original.jpg", photo.src?.original)
    }

    @Test
    fun testPexelsVideoJsonResponseParsing() {
        val json = """
            {
              "page": 1,
              "per_page": 15,
              "total_results": 50,
              "videos": [
                {
                  "id": 9999,
                  "width": 1280,
                  "height": 720,
                  "url": "https://www.pexels.com/video/9999/",
                  "image": "https://images.pexels.com/videos/9999/preview.jpg",
                  "duration": 15,
                  "user": {
                    "id": 88,
                    "name": "Jane Smith",
                    "url": "https://www.pexels.com/@janesmith"
                  },
                  "video_files": [
                    {
                      "id": 100,
                      "quality": "hd",
                      "file_type": "video/mp4",
                      "width": 1280,
                      "height": 720,
                      "link": "https://video.pexels.com/hd.mp4"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val gson = Gson()
        val response = gson.fromJson(json, PexelsVideoResponse::class.java)

        assertEquals(1, response.page)
        assertEquals(1, response.videos.size)

        val video = response.videos.first()
        assertEquals(9999L, video.id)
        assertEquals(15, video.duration)
        assertEquals("Jane Smith", video.user?.name)
        assertEquals("https://video.pexels.com/hd.mp4", video.videoFiles.first().link)
    }
}
