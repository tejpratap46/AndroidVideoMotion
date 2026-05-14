package com.tejpratapsingh.motionlib.templates

import com.google.gson.JsonObject

/**
 * Represents a Motion template.
 * @param sduiJson The raw SDUI JSON containing placeholders like {{variable_name}}.
 */
data class MotionTemplate(
    val sduiJson: JsonObject
)
