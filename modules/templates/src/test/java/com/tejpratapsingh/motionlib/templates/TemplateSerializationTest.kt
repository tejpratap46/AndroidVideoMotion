package com.tejpratapsingh.motionlib.templates

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.tejpratapsingh.motionlib.templates.json.JsonMotionTemplate
import com.tejpratapsingh.motionlib.templates.model.ParameterType
import com.tejpratapsingh.motionlib.templates.model.TemplateData
import com.tejpratapsingh.motionlib.templates.model.TemplateParameter
import com.tejpratapsingh.motionlib.templates.serialization.TemplateSerialization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateSerializationTest {
    @Test
    fun testPlaceholderReplacement() {
        val json =
            """
            {
                "type": "PopUpTextView",
                "text": "{{title}}",
                "duration": "{{duration}}",
                "color": "{{color}}",
                "nested": {
                    "key": "Value: {{title}}"
                }
            }
            """.trimIndent()

        val content = JsonParser.parseString(json).asJsonObject
        val data =
            TemplateData(
                mapOf(
                    "title" to "Hello World",
                    "duration" to 300,
                    "color" to 0xFF00FF,
                ),
            )

        val applied = TemplateSerialization.applyData(content, data).asJsonObject

        assertEquals("Hello World", applied.get("text").asString)
        assertEquals(300, applied.get("duration").asInt)
        assertEquals(0xFF00FF, applied.get("color").asInt)
        assertEquals("Value: Hello World", applied.getAsJsonObject("nested").get("key").asString)
    }

    @Test
    fun testTemplateSerialization() {
        val parameters =
            listOf(
                TemplateParameter("title", ParameterType.STRING, "Default"),
                TemplateParameter("duration", ParameterType.INTEGER, 100),
            )
        val content =
            JsonObject().apply {
                addProperty("type", "SimpleView")
                addProperty("text", "{{title}}")
            }

        val template = JsonMotionTemplate("MyTemplate", parameters, content)
        val json = TemplateSerialization.templateToJson(template)

        assertEquals("MyTemplate", json.get("name").asString)
        assertEquals(2, json.getAsJsonArray("parameters").size())
        assertTrue(json.has("content"))

        val restoredTemplate = TemplateSerialization.templateFromJson(json)
        assertEquals(template.name, restoredTemplate.name)
        assertEquals(template.parameters.size, restoredTemplate.parameters.size)
        assertEquals(template.rawContent, restoredTemplate.rawContent)
    }

    @Test
    fun testArrayReplication() {
        val json =
            """
            {
                "views": [
                    {
                        "type": "StaticView"
                    },
                    {
                        "{{REPLICATE}}": "items",
                        "template": {
                            "type": "DynamicView",
                            "text": "{{text}}",
                            "frame": "{{frame}}"
                        }
                    }
                ]
            }
            """.trimIndent()

        val content = JsonParser.parseString(json).asJsonObject
        val data =
            TemplateData(
                mapOf(
                    "items" to
                        listOf(
                            mapOf("text" to "Item 1", "frame" to 10),
                            mapOf("text" to "Item 2", "frame" to 20),
                        ),
                ),
            )

        val applied = TemplateSerialization.applyData(content, data).asJsonObject
        val views = applied.getAsJsonArray("views")

        assertEquals(3, views.size()) // 1 static + 2 dynamic

        val view0 = views.get(0).asJsonObject
        assertEquals("StaticView", view0.get("type").asString)

        val view1 = views.get(1).asJsonObject
        assertEquals("DynamicView", view1.get("type").asString)
        assertEquals("Item 1", view1.get("text").asString)
        assertEquals(10, view1.get("frame").asInt)

        val view2 = views.get(2).asJsonObject
        assertEquals("Item 2", view2.get("text").asString)
        assertEquals(20, view2.get("frame").asInt)
    }
}
