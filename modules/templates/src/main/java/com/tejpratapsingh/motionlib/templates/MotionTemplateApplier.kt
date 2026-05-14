package com.tejpratapsingh.motionlib.templates

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

/**
 * Applies data to a [MotionTemplate] by replacing placeholders in its SDUI JSON.
 */
object MotionTemplateApplier {

    private const val PLACEHOLDER_START = "{{"
    private const val PLACEHOLDER_END = "}}"

    private const val REPLICATE_KEY = "{{REPLICATE}}"
    private const val TEMPLATE_KEY = "template"

    /**
     * Applies data to the template JSON.
     * @param template The template to apply data to.
     * @param data A map of keys to values to replace placeholders.
     * @return A new [JsonObject] with placeholders replaced by actual data.
     */
    fun apply(template: MotionTemplate, data: Map<String, Any>): JsonObject {
        return applyToElement(template.sduiJson, data).asJsonObject
    }

    private fun applyToElement(element: JsonElement, data: Map<String, Any>): JsonElement {
        return when {
            element.isJsonObject -> {
                val jsonObj = element.asJsonObject
                val newObj = JsonObject()
                jsonObj.entrySet().forEach { (key, value) ->
                    newObj.add(key, applyToElement(value, data))
                }
                newObj
            }

            element.isJsonArray -> {
                val newArray = JsonArray()
                element.asJsonArray.forEach { value ->
                    if (value.isJsonObject && value.asJsonObject.has(REPLICATE_KEY)) {
                        // Handle list replication
                        val replicationObj = value.asJsonObject
                        val dataKey = replicationObj.get(REPLICATE_KEY).asString
                        val listTemplate = replicationObj.get(TEMPLATE_KEY)
                        
                        val listData = data[dataKey] as? List<*>
                        listData?.forEach { item ->
                            val itemMap = when (item) {
                                is Map<*, *> -> item.mapKeys { it.key.toString() }
                                else -> {
                                    // Try to convert object to map using reflection or keep as is if primitive
                                    // For simplicity in this example, we assume Map or we can use Gson to convert object to Map
                                    try {
                                        val json = com.google.gson.Gson().toJsonTree(item).asJsonObject
                                        json.entrySet().associate { it.key to it.value }
                                    } catch (e: Exception) {
                                        emptyMap<String, Any>()
                                    }
                                }
                            }
                            // Merge item data with parent data so globals are available
                            val mergedData = data + itemMap.filterValues { it != null }.mapValues { it.value!! }
                            newArray.add(applyToElement(listTemplate, mergedData))
                        }
                    } else {
                        newArray.add(applyToElement(value, data))
                    }
                }
                newArray
            }

            element.isJsonPrimitive -> {
                val primitive = element.asJsonPrimitive
                if (primitive.isString) {
                    val replacedString = replacePlaceholders(primitive.asString, data)
                    JsonPrimitive(replacedString)
                } else {
                    primitive
                }
            }

            else -> element
        }
    }

    private fun replacePlaceholders(input: String, data: Map<String, Any>): String {
        var result = input
        data.forEach { (key, value) ->
            val placeholder = "$PLACEHOLDER_START$key$PLACEHOLDER_END"
            result = result.replace(placeholder, value.toString())
        }
        return result
    }
}
