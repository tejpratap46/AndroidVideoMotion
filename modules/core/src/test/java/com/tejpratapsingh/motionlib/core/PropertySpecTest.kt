package com.tejpratapsingh.motionlib.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PropertySpecTest {

    @Test
    fun testPropertySpecHelpers() {
        val numberSpec = PropertySpec.number("testNum", label = "Test Number", min = 0, max = 10, isInteger = true)
        assertEquals("testNum", numberSpec.key)
        assertEquals("Test Number", numberSpec.label)
        assertEquals(PropertyViewType.NUMBER, numberSpec.viewType)
        assertEquals(0, numberSpec.min)
        assertEquals(10, numberSpec.max)
        assertTrue(numberSpec.isInteger)

        val sliderSpec = PropertySpec.slider("testSlider", label = "Test Slider", min = 0.0f, max = 1.0f, step = 0.1f)
        assertEquals(PropertyViewType.SLIDER, sliderSpec.viewType)
        assertEquals(0.0f, sliderSpec.min)
        assertEquals(1.0f, sliderSpec.max)

        val textSpec = PropertySpec.text("textKey", label = "Text Label")
        assertEquals(PropertyViewType.TEXT, textSpec.viewType)

        val dropdownSpec = PropertySpec.dropdown("enumKey", label = "Enum Label", options = listOf("A", "B", "C"))
        assertEquals(PropertyViewType.DROPDOWN, dropdownSpec.viewType)
        assertEquals(listOf("A", "B", "C"), dropdownSpec.options)

        val colorSpec = PropertySpec.color("colorKey", label = "Color Label")
        assertEquals(PropertyViewType.COLOR, colorSpec.viewType)
    }
}
