package com.tejpratapsingh.motionlib.templates.model

class TemplateData(private val values: Map<String, Any>) {
    @Suppress("UNCHECKED_CAST")
    fun <T> get(name: String): T? = values[name] as? T

    fun getString(name: String): String? = get(name)
    fun getInt(name: String): Int? = get(name)
    fun getFloat(name: String): Float? = get(name)
    fun getBoolean(name: String): Boolean? = get(name)
}
