package com.tejpratapsingh.motionlib.core

enum class PropertyViewType {
    NUMBER,    // Numeric input (+/- buttons and text field)
    SLIDER,    // Range slider + numeric text field
    TEXT,      // Text string input
    BOOLEAN,   // Switch / toggle
    DROPDOWN,  // Single-select dropdown menu
    COLOR      // Hex color field + Color Picker Dialog
}

data class PropertySpec(
    val key: String,
    val label: String = key,
    val viewType: PropertyViewType = PropertyViewType.TEXT,
    val min: Number? = null,
    val max: Number? = null,
    val step: Number? = null,
    val isInteger: Boolean = false,
    val options: List<String> = emptyList(),
    val defaultValue: Any? = null,
    val category: String = "GENERAL",
    val inheritable: Boolean = true
) {
    companion object {
        fun number(
            key: String,
            label: String = key,
            min: Number? = null,
            max: Number? = null,
            step: Number? = null,
            isInteger: Boolean = true,
            defaultValue: Any? = null,
            category: String = "GENERAL"
        ) = PropertySpec(
            key = key,
            label = label,
            viewType = PropertyViewType.NUMBER,
            min = min,
            max = max,
            step = step,
            isInteger = isInteger,
            defaultValue = defaultValue,
            category = category
        )

        fun slider(
            key: String,
            label: String = key,
            min: Number = 0f,
            max: Number = 1f,
            step: Number? = null,
            isInteger: Boolean = false,
            defaultValue: Any? = null,
            category: String = "GENERAL"
        ) = PropertySpec(
            key = key,
            label = label,
            viewType = PropertyViewType.SLIDER,
            min = min,
            max = max,
            step = step,
            isInteger = isInteger,
            defaultValue = defaultValue,
            category = category
        )

        fun text(
            key: String,
            label: String = key,
            defaultValue: Any? = null,
            category: String = "GENERAL"
        ) = PropertySpec(
            key = key,
            label = label,
            viewType = PropertyViewType.TEXT,
            defaultValue = defaultValue,
            category = category
        )

        fun boolean(
            key: String,
            label: String = key,
            defaultValue: Boolean = false,
            category: String = "GENERAL"
        ) = PropertySpec(
            key = key,
            label = label,
            viewType = PropertyViewType.BOOLEAN,
            defaultValue = defaultValue,
            category = category
        )

        fun dropdown(
            key: String,
            label: String = key,
            options: List<String>,
            defaultValue: String? = null,
            category: String = "GENERAL"
        ) = PropertySpec(
            key = key,
            label = label,
            viewType = PropertyViewType.DROPDOWN,
            options = options,
            defaultValue = defaultValue,
            category = category
        )

        fun color(
            key: String,
            label: String = key,
            defaultValue: String? = null,
            category: String = "GENERAL"
        ) = PropertySpec(
            key = key,
            label = label,
            viewType = PropertyViewType.COLOR,
            defaultValue = defaultValue,
            category = category
        )
    }
}
