package com.tejpratapsingh.motionlib.coil.infra

import android.graphics.PointF
import androidx.core.net.toUri
import com.commit451.coiltransformations.CropTransformation
import com.google.gson.JsonArray
import com.tejpratapsingh.motion.sdui.infra.MotionSdui
import com.tejpratapsingh.motion.sdui.infra.parseMotionEffectProps
import com.tejpratapsingh.motion.sdui.infra.parseMotionViewProps
import com.tejpratapsingh.motionlib.coil.effects.CoilBlurEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilBrightnessEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilCenterOnFaceEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilColorFilterEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilContrastEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilCropEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilGrayscaleEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilInvertEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilKuwaharaEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilMaskEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilPixelationEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilRoundedCornersEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilSepiaEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilSketchEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilSwirlEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilToonEffect
import com.tejpratapsingh.motionlib.coil.effects.CoilVignetteEffect
import com.tejpratapsingh.motionlib.coil.plugins.CoilBlurPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilBrightnessPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilCenterOnFacePlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilCircleCropPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilColorFilterPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilContrastPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilCropPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilGrayscalePlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilInvertPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilKuwaharaPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilMaskPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilPixelationPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilRoundedCornersPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilSepiaPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilSketchPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilSquareCropPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilSwirlPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilToonPlugin
import com.tejpratapsingh.motionlib.coil.plugins.CoilVignettePlugin
import com.tejpratapsingh.motionlib.coil.video.CoilVideoPlayer

/**
 * Initializer for Coil-based SDUI components.
 */
object CoilMotionSduiInitializer {
    fun initialize() {
        // --- Plugins ---

        // Register CoilBlurPlugin
        MotionSdui.registerPlugin(CoilBlurPlugin::class.java.simpleName) { context, json ->
            val radius = json.get("radius")?.asFloat ?: 10f
            val sampling = json.get("sampling")?.asFloat ?: 1f
            CoilBlurPlugin(context, radius, sampling)
        }
        MotionSdui.registerPluginSerializer(CoilBlurPlugin::class.java) { plugin, json ->
            json.addProperty("radius", plugin.radius)
            json.addProperty("sampling", plugin.sampling)
        }

        // Register CoilGrayscalePlugin
        MotionSdui.registerPlugin(CoilGrayscalePlugin::class.java.simpleName) { _, _ ->
            CoilGrayscalePlugin()
        }
        MotionSdui.registerPluginSerializer(CoilGrayscalePlugin::class.java) { _, _ -> }

        // Register CoilColorFilterPlugin
        MotionSdui.registerPlugin(CoilColorFilterPlugin::class.java.simpleName) { _, json ->
            val color = json.get("color")?.asInt ?: 0
            CoilColorFilterPlugin(color)
        }
        MotionSdui.registerPluginSerializer(CoilColorFilterPlugin::class.java) { plugin, json ->
            json.addProperty("color", plugin.color)
        }

        // Register CoilRoundedCornersPlugin
        MotionSdui.registerPlugin(CoilRoundedCornersPlugin::class.java.simpleName) { _, json ->
            val radius = json.get("radius")?.asFloat ?: 0f
            CoilRoundedCornersPlugin(radius)
        }
        MotionSdui.registerPluginSerializer(CoilRoundedCornersPlugin::class.java) { plugin, json ->
            json.addProperty("radius", plugin.radius)
        }

        // Register CoilCircleCropPlugin
        MotionSdui.registerPlugin(CoilCircleCropPlugin::class.java.simpleName) { _, _ ->
            CoilCircleCropPlugin()
        }
        MotionSdui.registerPluginSerializer(CoilCircleCropPlugin::class.java) { _, _ -> }

        // Register CoilSquareCropPlugin
        MotionSdui.registerPlugin(CoilSquareCropPlugin::class.java.simpleName) { _, _ ->
            CoilSquareCropPlugin()
        }
        MotionSdui.registerPluginSerializer(CoilSquareCropPlugin::class.java) { _, _ -> }

        // Register CoilCropPlugin
        MotionSdui.registerPlugin(CoilCropPlugin::class.java.simpleName) { _, json ->
            val typeStr = json.get("cropType")?.asString ?: "CENTER"
            val type =
                try {
                    CropTransformation.CropType.valueOf(typeStr)
                } catch (e: Exception) {
                    CropTransformation.CropType.CENTER
                }
            CoilCropPlugin(type)
        }
        MotionSdui.registerPluginSerializer(CoilCropPlugin::class.java) { plugin, json ->
            json.addProperty("cropType", plugin.type.name)
        }

        // Register CoilMaskPlugin
        MotionSdui.registerPlugin(CoilMaskPlugin::class.java.simpleName) { context, json ->
            val maskId = json.get("maskId")?.asInt ?: 0
            CoilMaskPlugin(context, maskId)
        }
        MotionSdui.registerPluginSerializer(CoilMaskPlugin::class.java) { plugin, json ->
            json.addProperty("maskId", plugin.maskId)
        }

        // Register CoilBrightnessPlugin
        MotionSdui.registerPlugin(CoilBrightnessPlugin::class.java.simpleName) { context, json ->
            val brightness = json.get("brightness")?.asFloat ?: 0.0f
            CoilBrightnessPlugin(context, brightness)
        }
        MotionSdui.registerPluginSerializer(CoilBrightnessPlugin::class.java) { plugin, json ->
            json.addProperty("brightness", plugin.brightness)
        }

        // Register CoilContrastPlugin
        MotionSdui.registerPlugin(CoilContrastPlugin::class.java.simpleName) { context, json ->
            val contrast = json.get("contrast")?.asFloat ?: 1.0f
            CoilContrastPlugin(context, contrast)
        }
        MotionSdui.registerPluginSerializer(CoilContrastPlugin::class.java) { plugin, json ->
            json.addProperty("contrast", plugin.contrast)
        }

        // Register CoilInvertPlugin
        MotionSdui.registerPlugin(CoilInvertPlugin::class.java.simpleName) { context, _ ->
            CoilInvertPlugin(context)
        }
        MotionSdui.registerPluginSerializer(CoilInvertPlugin::class.java) { _, _ -> }

        // Register CoilKuwaharaPlugin
        MotionSdui.registerPlugin(CoilKuwaharaPlugin::class.java.simpleName) { context, json ->
            val radius = json.get("radius")?.asInt ?: 25
            CoilKuwaharaPlugin(context, radius)
        }
        MotionSdui.registerPluginSerializer(CoilKuwaharaPlugin::class.java) { plugin, json ->
            json.addProperty("radius", plugin.radius)
        }

        // Register CoilPixelationPlugin
        MotionSdui.registerPlugin(CoilPixelationPlugin::class.java.simpleName) { context, json ->
            val pixel = json.get("pixel")?.asFloat ?: 10f
            CoilPixelationPlugin(context, pixel)
        }
        MotionSdui.registerPluginSerializer(CoilPixelationPlugin::class.java) { plugin, json ->
            json.addProperty("pixel", plugin.pixel)
        }

        // Register CoilSepiaPlugin
        MotionSdui.registerPlugin(CoilSepiaPlugin::class.java.simpleName) { context, _ ->
            CoilSepiaPlugin(context)
        }
        MotionSdui.registerPluginSerializer(CoilSepiaPlugin::class.java) { _, _ -> }

        // Register CoilSketchPlugin
        MotionSdui.registerPlugin(CoilSketchPlugin::class.java.simpleName) { context, _ ->
            CoilSketchPlugin(context)
        }
        MotionSdui.registerPluginSerializer(CoilSketchPlugin::class.java) { _, _ -> }

        // Register CoilSwirlPlugin
        MotionSdui.registerPlugin(CoilSwirlPlugin::class.java.simpleName) { context, json ->
            val radius = json.get("radius")?.asFloat ?: 0.5f
            val angle = json.get("angle")?.asFloat ?: 1.0f
            val centerX = json.get("centerX")?.asFloat ?: 0.5f
            val centerY = json.get("centerY")?.asFloat ?: 0.5f
            CoilSwirlPlugin(context, radius, angle, PointF(centerX, centerY))
        }
        MotionSdui.registerPluginSerializer(CoilSwirlPlugin::class.java) { plugin, json ->
            json.addProperty("radius", plugin.radius)
            json.addProperty("angle", plugin.angle)
            json.addProperty("centerX", plugin.center.x)
            json.addProperty("centerY", plugin.center.y)
        }

        // Register CoilToonPlugin
        MotionSdui.registerPlugin(CoilToonPlugin::class.java.simpleName) { context, json ->
            val threshold = json.get("threshold")?.asFloat ?: 0.2f
            val quantizationLevels = json.get("quantizationLevels")?.asFloat ?: 10.0f
            CoilToonPlugin(context, threshold, quantizationLevels)
        }
        MotionSdui.registerPluginSerializer(CoilToonPlugin::class.java) { plugin, json ->
            json.addProperty("threshold", plugin.threshold)
            json.addProperty("quantizationLevels", plugin.quantizationLevels)
        }

        // Register CoilVignettePlugin
        MotionSdui.registerPlugin(CoilVignettePlugin::class.java.simpleName) { context, json ->
            val centerX = json.get("centerX")?.asFloat ?: 0.5f
            val centerY = json.get("centerY")?.asFloat ?: 0.5f
            val start = json.get("start")?.asFloat ?: 0.0f
            val end = json.get("end")?.asFloat ?: 0.75f
            val colorJson = json.getAsJsonArray("color")
            val color =
                if (colorJson != null) {
                    FloatArray(colorJson.size()) { colorJson.get(it).asFloat }
                } else {
                    floatArrayOf(0.0f, 0.0f, 0.0f)
                }
            CoilVignettePlugin(context, PointF(centerX, centerY), color, start, end)
        }
        MotionSdui.registerPluginSerializer(CoilVignettePlugin::class.java) { plugin, json ->
            json.addProperty("centerX", plugin.center.x)
            json.addProperty("centerY", plugin.center.y)
            json.addProperty("start", plugin.start)
            json.addProperty("end", plugin.end)
            val colorArray = JsonArray()
            plugin.color.forEach { colorArray.add(it) }
            json.add("color", colorArray)
        }

        // Register CoilCenterOnFacePlugin
        MotionSdui.registerPlugin(CoilCenterOnFacePlugin::class.java.simpleName) { _, json ->
            val zoom = json.get("zoom")?.asInt ?: 100
            CoilCenterOnFacePlugin(zoom)
        }
        MotionSdui.registerPluginSerializer(CoilCenterOnFacePlugin::class.java) { plugin, json ->
            json.addProperty("zoom", plugin.zoom)
        }

        // --- Views ---

        // Register CoilVideoPlayer
        MotionSdui.registerView(CoilVideoPlayer::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps()
            val videoUriStr =
                json.get("videoUri")?.asString
                    ?: throw IllegalArgumentException("videoUri required for CoilVideoPlayer")
            CoilVideoPlayer(
                context = context,
                videoUri = videoUriStr.toUri(),
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(CoilVideoPlayer::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("videoUri", view.videoUri.toString())
        }

        // --- Effects ---

        // Register CoilBlurEffect
        MotionSdui.registerEffect(CoilBlurEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val radius = json.get("radius")?.asFloat ?: 10f
            val sampling = json.get("sampling")?.asFloat ?: 1f
            CoilBlurEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                radius = radius,
                sampling = sampling,
            )
        }
        MotionSdui.registerEffectSerializer(CoilBlurEffect::class.java) { effect, json ->
            json.addProperty("radius", effect.radius)
            json.addProperty("sampling", effect.sampling)
        }

        // Register CoilGrayscaleEffect
        MotionSdui.registerEffect(CoilGrayscaleEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            CoilGrayscaleEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
            )
        }
        MotionSdui.registerEffectSerializer(CoilGrayscaleEffect::class.java) { _, _ -> }

        // Register CoilColorFilterEffect
        MotionSdui.registerEffect(CoilColorFilterEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val color = json.get("color")?.asInt ?: 0
            CoilColorFilterEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                color = color,
            )
        }
        MotionSdui.registerEffectSerializer(CoilColorFilterEffect::class.java) { effect, json ->
            json.addProperty("color", effect.color)
        }

        // Register CoilRoundedCornersEffect
        MotionSdui.registerEffect(CoilRoundedCornersEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val radius = json.get("radius")?.asFloat ?: 0f
            CoilRoundedCornersEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                radius = radius,
            )
        }
        MotionSdui.registerEffectSerializer(CoilRoundedCornersEffect::class.java) { effect, json ->
            json.addProperty("radius", effect.radius)
        }

        // Register CoilCropEffect
        MotionSdui.registerEffect(CoilCropEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val typeStr = json.get("cropType")?.asString ?: "CENTER"
            val type =
                try {
                    CropTransformation.CropType.valueOf(typeStr)
                } catch (e: Exception) {
                    CropTransformation.CropType.CENTER
                }
            CoilCropEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                type = type,
            )
        }
        MotionSdui.registerEffectSerializer(CoilCropEffect::class.java) { effect, json ->
            json.addProperty("cropType", effect.type.name)
        }

        // Register CoilMaskEffect
        MotionSdui.registerEffect(CoilMaskEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val maskId = json.get("maskId")?.asInt ?: 0
            CoilMaskEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                maskId = maskId,
            )
        }
        MotionSdui.registerEffectSerializer(CoilMaskEffect::class.java) { effect, json ->
            json.addProperty("maskId", effect.maskId)
        }

        // Register CoilBrightnessEffect
        MotionSdui.registerEffect(CoilBrightnessEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val brightness = json.get("brightness")?.asFloat ?: 0.0f
            CoilBrightnessEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                brightness = brightness,
            )
        }
        MotionSdui.registerEffectSerializer(CoilBrightnessEffect::class.java) { effect, json ->
            json.addProperty("brightness", effect.brightness)
        }

        // Register CoilContrastEffect
        MotionSdui.registerEffect(CoilContrastEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val contrast = json.get("contrast")?.asFloat ?: 1.0f
            CoilContrastEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                contrast = contrast,
            )
        }
        MotionSdui.registerEffectSerializer(CoilContrastEffect::class.java) { effect, json ->
            json.addProperty("contrast", effect.contrast)
        }

        // Register CoilInvertEffect
        MotionSdui.registerEffect(CoilInvertEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            CoilInvertEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
            )
        }
        MotionSdui.registerEffectSerializer(CoilInvertEffect::class.java) { _, _ -> }

        // Register CoilKuwaharaEffect
        MotionSdui.registerEffect(CoilKuwaharaEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val radius = json.get("radius")?.asInt ?: 25
            CoilKuwaharaEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                radius = radius,
            )
        }
        MotionSdui.registerEffectSerializer(CoilKuwaharaEffect::class.java) { effect, json ->
            json.addProperty("radius", effect.radius)
        }

        // Register CoilPixelationEffect
        MotionSdui.registerEffect(CoilPixelationEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val pixel = json.get("pixel")?.asFloat ?: 10f
            CoilPixelationEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                pixel = pixel,
            )
        }
        MotionSdui.registerEffectSerializer(CoilPixelationEffect::class.java) { effect, json ->
            json.addProperty("pixel", effect.pixel)
        }

        // Register CoilSepiaEffect
        MotionSdui.registerEffect(CoilSepiaEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            CoilSepiaEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
            )
        }
        MotionSdui.registerEffectSerializer(CoilSepiaEffect::class.java) { _, _ -> }

        // Register CoilSketchEffect
        MotionSdui.registerEffect(CoilSketchEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            CoilSketchEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
            )
        }
        MotionSdui.registerEffectSerializer(CoilSketchEffect::class.java) { _, _ -> }

        // Register CoilSwirlEffect
        MotionSdui.registerEffect(CoilSwirlEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val radius = json.get("radius")?.asFloat ?: 0.5f
            val angle = json.get("angle")?.asFloat ?: 1.0f
            val centerX = json.get("centerX")?.asFloat ?: 0.5f
            val centerY = json.get("centerY")?.asFloat ?: 0.5f
            CoilSwirlEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                radius = radius,
                angle = angle,
                center = PointF(centerX, centerY),
            )
        }
        MotionSdui.registerEffectSerializer(CoilSwirlEffect::class.java) { effect, json ->
            json.addProperty("radius", effect.radius)
            json.addProperty("angle", effect.angle)
            json.addProperty("centerX", effect.center.x)
            json.addProperty("centerY", effect.center.y)
        }

        // Register CoilToonEffect
        MotionSdui.registerEffect(CoilToonEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val threshold = json.get("threshold")?.asFloat ?: 0.2f
            val quantizationLevels = json.get("quantizationLevels")?.asFloat ?: 10.0f
            CoilToonEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                threshold = threshold,
                quantizationLevels = quantizationLevels,
            )
        }
        MotionSdui.registerEffectSerializer(CoilToonEffect::class.java) { effect, json ->
            json.addProperty("threshold", effect.threshold)
            json.addProperty("quantizationLevels", effect.quantizationLevels)
        }

        // Register CoilVignetteEffect
        MotionSdui.registerEffect(CoilVignetteEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val centerX = json.get("centerX")?.asFloat ?: 0.5f
            val centerY = json.get("centerY")?.asFloat ?: 0.5f
            val start = json.get("start")?.asFloat ?: 0.0f
            val end = json.get("end")?.asFloat ?: 0.75f
            val colorJson = json.getAsJsonArray("color")
            val color =
                if (colorJson != null) {
                    FloatArray(colorJson.size()) { colorJson.get(it).asFloat }
                } else {
                    floatArrayOf(0.0f, 0.0f, 0.0f)
                }
            CoilVignetteEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                center = PointF(centerX, centerY),
                color = color,
                start = start,
                end = end,
            )
        }
        MotionSdui.registerEffectSerializer(CoilVignetteEffect::class.java) { effect, json ->
            json.addProperty("centerX", effect.center.x)
            json.addProperty("centerY", effect.center.y)
            json.addProperty("start", effect.start)
            json.addProperty("end", effect.end)
            val colorArray = JsonArray()
            effect.color.forEach { colorArray.add(it) }
            json.add("color", colorArray)
        }

        // Register CoilCenterOnFaceEffect
        MotionSdui.registerEffect(CoilCenterOnFaceEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val zoom = json.get("zoom")?.asInt ?: 100
            CoilCenterOnFaceEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                zoom = zoom,
            )
        }
        MotionSdui.registerEffectSerializer(CoilCenterOnFaceEffect::class.java) { effect, json ->
            json.addProperty("zoom", effect.zoom)
        }
    }
}
