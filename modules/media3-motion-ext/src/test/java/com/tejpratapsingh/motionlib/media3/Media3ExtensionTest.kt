package com.tejpratapsingh.motionlib.media3

import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.media3.effects.BrightnessEffect
import com.tejpratapsingh.motionlib.media3.effects.ContrastEffect
import com.tejpratapsingh.motionlib.media3.effects.GrayscaleEffect
import com.tejpratapsingh.motionlib.media3.effects.InvertEffect
import com.tejpratapsingh.motionlib.media3.effects.RgbEffect
import com.tejpratapsingh.motionlib.media3.plugins.BrightnessPlugin
import com.tejpratapsingh.motionlib.media3.plugins.ContrastPlugin
import com.tejpratapsingh.motionlib.media3.plugins.GrayscalePlugin
import com.tejpratapsingh.motionlib.media3.plugins.InvertPlugin
import com.tejpratapsingh.motionlib.media3.plugins.RgbPlugin
import org.junit.Assert.assertTrue
import org.junit.Test

class Media3ExtensionTest {

    @Test
    fun testEffectsImplementation() {
        val brightnessEffect = BrightnessEffect(0, 10)
        assertTrue(brightnessEffect is MotionEffect)

        val contrastEffect = ContrastEffect(0, 10)
        assertTrue(contrastEffect is MotionEffect)

        val grayscaleEffect = GrayscaleEffect(0, 10)
        assertTrue(grayscaleEffect is MotionEffect)

        val invertEffect = InvertEffect(0, 10)
        assertTrue(invertEffect is MotionEffect)

        val rgbEffect = RgbEffect(0, 10)
        assertTrue(rgbEffect is MotionEffect)
    }

    @Test
    fun testPluginsImplementation() {
        val brightnessPlugin = BrightnessPlugin(0.5f)
        assertTrue(brightnessPlugin is MotionPlugin)

        val contrastPlugin = ContrastPlugin(1.5f)
        assertTrue(contrastPlugin is MotionPlugin)

        val grayscalePlugin = GrayscalePlugin()
        assertTrue(grayscalePlugin is MotionPlugin)

        val invertPlugin = InvertPlugin()
        assertTrue(invertPlugin is MotionPlugin)

        val rgbPlugin = RgbPlugin(redScale = 1.2f)
        assertTrue(rgbPlugin is MotionPlugin)
    }
}
