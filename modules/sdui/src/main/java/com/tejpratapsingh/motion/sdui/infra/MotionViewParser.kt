package com.tejpratapsingh.motion.sdui.infra

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.IComposerView

/**
 * Polymorphic serialization for [MotionView].
 */
fun MotionView.toJson(): JsonObject {
    val json = JsonObject()
    json.addProperty("type", this.javaClass.simpleName)
    json.addProperty("startFrame", startFrame)
    json.addProperty("endFrame", endFrame)

    val loopJson = JsonObject()
    loopJson.addProperty("start", loop.first)
    loopJson.addProperty("end", loop.second)
    json.add("loop", loopJson)

    json.add("layout", layoutInfo.toJson())

    if (effects.isNotEmpty()) {
        val effectsArray = JsonArray()
        effects.forEach { effect ->
            effectsArray.add(effect.toJson())
        }
        json.add("effects", effectsArray)
    }

    if (assets.isNotEmpty()) {
        val assetsArray = JsonArray()
        assets.forEach { asset ->
            assetsArray.add(asset.toJson())
        }
        json.add("assets", assetsArray)
    }

    if (this is IComposerView) {
        val pluginsArray = JsonArray()
        plugins.forEach { plugin ->
            pluginsArray.add(plugin.toJson())
        }
        if (pluginsArray.size() > 0) {
            json.add("plugins", pluginsArray)
        }
    }

    if (this is ViewGroup) {
        val childrenArray = JsonArray()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is MotionView) {
                childrenArray.add(child.toJson())
            }
        }
        if (childrenArray.size() > 0) {
            json.add("children", childrenArray)
        }
    }

    // Allow concrete implementations to add their own properties
    val serializer = MotionSdui.getViewSerializer(this.javaClass)
    serializer?.serialize(this, json)

    return json
}

/**
 * Polymorphic deserialization for [MotionView].
 */
fun JsonObject.toMotionView(context: Context): MotionView {
    val type = get("type")?.asString ?: throw IllegalArgumentException("Missing 'type' in MotionView JSON")
    val factory = MotionSdui.getViewFactory(type) ?: throw IllegalArgumentException("No factory registered for MotionView type: $type")

    val motionView = factory.create(context, this)

    // Check if the factory already handled effects via parseMotionViewProps or similar.
    // If effects list is empty, try to populate it from JSON.
    if (motionView.effects.isEmpty() && has("effects")) {
        val effectsArray = get("effects").asJsonArray
        effectsArray.forEach { effectJson ->
            if (effectJson is JsonObject) {
                motionView.addEffect(effectJson.toMotionEffect())
            }
        }
    }

    return motionView
}

/**
 * Helper to parse common [MotionView] properties.
 */
fun JsonObject.parseMotionViewProps(context: Context): MotionViewProps {
    val startFrame = get("startFrame")?.asInt ?: 0
    val endFrame = get("endFrame")?.asInt ?: 0
    val loop =
        if (has("loop")) {
            val loopObj = get("loop").asJsonObject
            Pair(loopObj.get("start").asInt, loopObj.get("end").asInt)
        } else {
            Pair(0, 0)
        }

    val effects = mutableListOf<MotionEffect>()
    if (has("effects")) {
        val effectsArray = get("effects").asJsonArray
        effectsArray.forEach { effectJson ->
            if (effectJson is JsonObject) {
                effects.add(effectJson.toMotionEffect())
            }
        }
    }

    val layoutInfo =
        if (has("layout")) {
            get("layout").asJsonObject.toLayoutInfo()
        } else {
            MotionLayoutInfo()
        }

    val assets = mutableListOf<MotionAsset>()
    if (has("assets")) {
        val assetsArray = get("assets").asJsonArray
        assetsArray.forEach { assetJson ->
            if (assetJson is JsonObject) {
                assets.add(assetJson.toMotionAsset(context))
            }
        }
    }

    return MotionViewProps(startFrame, endFrame, loop, effects, layoutInfo, assets)
}

data class MotionViewProps(
    val startFrame: Int,
    val endFrame: Int,
    val loop: Pair<Int, Int>,
    val effects: List<MotionEffect>,
    val layoutInfo: MotionLayoutInfo,
    val assets: List<MotionAsset>,
)

/**
 * Serialization for [MotionLayoutInfo].
 */
fun MotionLayoutInfo.toJson(): JsonObject {
    val json = JsonObject()

    val widthStr =
        when (width) {
            MotionLayoutInfo.MATCH_PARENT -> "match_parent"
            MotionLayoutInfo.WRAP_CONTENT -> "wrap_content"
            else -> width.toString()
        }
    json.addProperty("width", widthStr)

    val heightStr =
        when (height) {
            MotionLayoutInfo.MATCH_PARENT -> "match_parent"
            MotionLayoutInfo.WRAP_CONTENT -> "wrap_content"
            else -> height.toString()
        }
    json.addProperty("height", heightStr)

    val paddingJson = JsonObject()
    paddingJson.addProperty("left", padding.left)
    paddingJson.addProperty("top", padding.top)
    paddingJson.addProperty("right", padding.right)
    paddingJson.addProperty("bottom", padding.bottom)
    json.add("padding", paddingJson)

    val marginJson = JsonObject()
    marginJson.addProperty("left", margin.left)
    marginJson.addProperty("top", margin.top)
    marginJson.addProperty("right", margin.right)
    marginJson.addProperty("bottom", margin.bottom)
    json.add("margin", marginJson)

    if (gravity != Gravity.NO_GRAVITY) {
        json.addProperty("gravity", gravity.toGravityString())
    }

    return json
}

/**
 * Deserialization for [MotionLayoutInfo].
 */
fun JsonObject.toLayoutInfo(): MotionLayoutInfo {
    val width =
        if (has("width")) {
            val w = get("width").asString
            when (w) {
                "match_parent" -> MotionLayoutInfo.MATCH_PARENT
                "wrap_content" -> MotionLayoutInfo.WRAP_CONTENT
                else -> w.toIntOrNull() ?: MotionLayoutInfo.WRAP_CONTENT
            }
        } else {
            MotionLayoutInfo.WRAP_CONTENT
        }

    val height =
        if (has("height")) {
            val h = get("height").asString
            when (h) {
                "match_parent" -> MotionLayoutInfo.MATCH_PARENT
                "wrap_content" -> MotionLayoutInfo.WRAP_CONTENT
                else -> h.toIntOrNull() ?: MotionLayoutInfo.WRAP_CONTENT
            }
        } else {
            MotionLayoutInfo.WRAP_CONTENT
        }

    val padding =
        if (has("padding")) {
            val p = get("padding").asJsonObject
            MotionLayoutInfo.Padding(
                left = p.get("left")?.asInt ?: 0,
                top = p.get("top")?.asInt ?: 0,
                right = p.get("right")?.asInt ?: 0,
                bottom = p.get("bottom")?.asInt ?: 0,
            )
        } else {
            MotionLayoutInfo.Padding()
        }

    val margin =
        if (has("margin")) {
            val m = get("margin").asJsonObject
            MotionLayoutInfo.Margin(
                left = m.get("left")?.asInt ?: 0,
                top = m.get("top")?.asInt ?: 0,
                right = m.get("right")?.asInt ?: 0,
                bottom = m.get("bottom")?.asInt ?: 0,
            )
        } else {
            MotionLayoutInfo.Margin()
        }

    val gravity =
        if (has("gravity")) {
            get("gravity").asString.toGravityInt()
        } else {
            Gravity.NO_GRAVITY
        }

    return MotionLayoutInfo(width, height, padding, margin, gravity)
}

private fun Int.toGravityString(): String {
    val parts = mutableListOf<String>()
    if (this and Gravity.AXIS_PULL_BEFORE == Gravity.AXIS_PULL_BEFORE) {
        if (this and Gravity.RELATIVE_LAYOUT_DIRECTION == Gravity.RELATIVE_LAYOUT_DIRECTION) {
            parts.add("start")
        } else {
            parts.add("left")
        }
    }
    if (this and Gravity.AXIS_PULL_AFTER == Gravity.AXIS_PULL_AFTER) {
        if (this and Gravity.RELATIVE_LAYOUT_DIRECTION == Gravity.RELATIVE_LAYOUT_DIRECTION) {
            parts.add("end")
        } else {
            parts.add("right")
        }
    }
    if (this and Gravity.TOP == Gravity.TOP) parts.add("top")
    if (this and Gravity.BOTTOM == Gravity.BOTTOM) parts.add("bottom")
    if (this and Gravity.CENTER == Gravity.CENTER) {
        parts.add("center")
    } else {
        if (this and Gravity.CENTER_HORIZONTAL == Gravity.CENTER_HORIZONTAL) parts.add("center_horizontal")
        if (this and Gravity.CENTER_VERTICAL == Gravity.CENTER_VERTICAL) parts.add("center_vertical")
    }
    return parts.joinToString("|")
}

private fun String.toGravityInt(): Int {
    var gravity = Gravity.NO_GRAVITY
    this.split("|").forEach { part ->
        when (part.trim().lowercase()) {
            "top" -> gravity = gravity or Gravity.TOP
            "bottom" -> gravity = gravity or Gravity.BOTTOM
            "left" -> gravity = gravity or Gravity.LEFT
            "right" -> gravity = gravity or Gravity.RIGHT
            "start" -> gravity = gravity or Gravity.START
            "end" -> gravity = gravity or Gravity.END
            "center" -> gravity = gravity or Gravity.CENTER
            "center_horizontal" -> gravity = gravity or Gravity.CENTER_HORIZONTAL
            "center_vertical" -> gravity = gravity or Gravity.CENTER_VERTICAL
        }
    }
    return gravity
}
