package com.tejpratapsingh.motionlib.templates.serialization

import com.google.gson.*
import com.tejpratapsingh.motionlib.templates.json.JsonMotionTemplate
import com.tejpratapsingh.motionlib.templates.model.*
import java.util.regex.Pattern

object TemplateSerialization {
    private val GSON = GsonBuilder().setPrettyPrinting().create()
    private val PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}")

    fun templateToJson(template: MotionTemplate): JsonObject {
        val json = JsonObject()
        json.addProperty("name", template.name)
        
        val paramsArray = JsonArray()
        template.parameters.forEach { param ->
            paramsArray.add(parameterToJson(param))
        }
        json.add("parameters", paramsArray)
        
        if (template is JsonMotionTemplate) {
            json.add("content", template.rawContent)
        }
        
        return json
    }

    fun templateFromJson(json: JsonObject): JsonMotionTemplate {
        val name = json.get("name").asString
        val paramsArray = json.getAsJsonArray("parameters")
        val parameters = paramsArray.map { parameterFromJson(it.asJsonObject) }
        val content = json.getAsJsonObject("content")
        
        return JsonMotionTemplate(name, parameters, content)
    }

    private fun parameterToJson(parameter: TemplateParameter<*>): JsonObject {
        val json = JsonObject()
        json.addProperty("name", parameter.name)
        json.addProperty("type", parameter.type.name)
        if (parameter.defaultValue != null) {
            json.add("defaultValue", GSON.toJsonTree(parameter.defaultValue))
        }
        if (parameter.description != null) {
            json.addProperty("description", parameter.description)
        }
        return json
    }

    private fun parameterFromJson(json: JsonObject): TemplateParameter<*> {
        val name = json.get("name").asString
        val type = ParameterType.valueOf(json.get("type").asString)
        val description = json.get("description")?.asString
        val defaultValueElement = json.get("defaultValue")
        
        return when (type) {
            ParameterType.STRING -> TemplateParameter(name, type, defaultValueElement?.asString, description)
            ParameterType.INTEGER -> TemplateParameter(name, type, defaultValueElement?.asInt, description)
            ParameterType.FLOAT -> TemplateParameter(name, type, defaultValueElement?.asFloat, description)
            ParameterType.COLOR -> TemplateParameter(name, type, defaultValueElement?.asInt, description)
            ParameterType.BOOLEAN -> TemplateParameter(name, type, defaultValueElement?.asBoolean, description)
            ParameterType.IMAGE -> TemplateParameter(name, type, defaultValueElement?.asString, description)
            ParameterType.VIDEO -> TemplateParameter(name, type, defaultValueElement?.asString, description)
        }
    }

    fun applyData(element: JsonElement, data: TemplateData): JsonElement {
        return when {
            element.isJsonObject -> {
                val obj = element.asJsonObject
                if (obj.has("{{REPLICATE}}")) {
                    // This object is a replication marker, but it should be handled by the parent array
                    // If it's encountered here, it means it's not inside an array or misused.
                    // We'll return it as is or handle it if we want to support single-item replication
                    element
                } else {
                    val newObj = JsonObject()
                    obj.entrySet().forEach { (key, value) ->
                        newObj.add(key, applyData(value, data))
                    }
                    newObj
                }
            }
            element.isJsonArray -> {
                val array = element.asJsonArray
                val newArray = JsonArray()
                array.forEach { item ->
                    if (item.isJsonObject && item.asJsonObject.has("{{REPLICATE}}")) {
                        val replicateObj = item.asJsonObject
                        val listKey = replicateObj.get("{{REPLICATE}}").asString
                        val template = replicateObj.get("template") ?: return@forEach
                        
                        val listData = data.get<List<Map<String, Any>>>(listKey)
                        listData?.forEach { itemData ->
                            val itemTemplateData = TemplateData(itemData)
                            newArray.add(applyData(template, itemTemplateData))
                        }
                    } else {
                        newArray.add(applyData(item, data))
                    }
                }
                newArray
            }
            element.isJsonPrimitive -> {
                val primitive = element.asJsonPrimitive
                if (primitive.isString) {
                    val text = primitive.asString
                    val matcher = PLACEHOLDER_PATTERN.matcher(text)
                    if (matcher.matches()) {
                        // Exact match, try to preserve type
                        val paramName = matcher.group(1) ?: ""
                        val value = data.get<Any>(paramName)
                        if (value != null) {
                            GSON.toJsonTree(value)
                        } else {
                            element
                        }
                    } else if (matcher.find()) {
                        // Partial match, string interpolation
                        val sb = StringBuffer()
                        matcher.reset()
                        while (matcher.find()) {
                            val paramName = matcher.group(1) ?: ""
                            val value = data.get<Any>(paramName)?.toString() ?: matcher.group(0)
                            matcher.appendReplacement(sb, value)
                        }
                        matcher.appendTail(sb)
                        JsonPrimitive(sb.toString())
                    } else {
                        element
                    }
                } else {
                    element
                }
            }
            else -> element
        }
    }
}
