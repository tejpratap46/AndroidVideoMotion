package com.tejpratapsingh.motionlib.templates.model

enum class ParameterType {
    STRING, INTEGER, FLOAT, COLOR, BOOLEAN, IMAGE, VIDEO
}

data class TemplateParameter<T>(
    val name: String,
    val type: ParameterType,
    val defaultValue: T? = null,
    val description: String? = null
)
