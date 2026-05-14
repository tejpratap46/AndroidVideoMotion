package com.tejpratapsingh.motionlib.templates

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Test

class MotionTemplateApplierTest {

    @Test
    fun testSimplePlaceholderReplacement() {
        val rawJson = """
            {
                "type": "text",
                "content": "{{text_content}}",
                "color": "{{color_code}}"
            }
        """.trimIndent()
        
        val template = MotionTemplate(JsonParser.parseString(rawJson).asJsonObject)
        val data = mapOf(
            "text_content" to "Hello World",
            "color" to "Red" // This won't match color_code exactly unless we handle partial matches or the key is exact
        )
        
        // Let's use exact keys for the test
        val exactData = mapOf(
            "text_content" to "Hello World",
            "color_code" to "#FF0000"
        )

        val result = MotionTemplateApplier.apply(template, exactData)
        
        assertEquals("Hello World", result.get("content").asString)
        assertEquals("#FF0000", result.get("color").asString)
    }

    @Test
    fun testListReplication() {
        val rawJson = """
            {
                "views": [
                    {
                        "{{REPLICATE}}": "items",
                        "template": {
                            "type": "text",
                            "content": "Item {{id}}: {{name}}"
                        }
                    }
                ]
            }
        """.trimIndent()

        val template = MotionTemplate(JsonParser.parseString(rawJson).asJsonObject)
        val data = mapOf(
            "items" to listOf(
                mapOf("id" to 1, "name" to "Alpha"),
                mapOf("id" to 2, "name" to "Beta")
            )
        )

        val result = MotionTemplateApplier.apply(template, data)
        val views = result.getAsJsonArray("views")

        assertEquals(2, views.size())
        assertEquals("Item 1: Alpha", views[0].asJsonObject.get("content").asString)
        assertEquals("Item 2: Beta", views[1].asJsonObject.get("content").asString)
    }
}
