package com.tejpratapsingh.motioneditor.utils

import android.content.Context
import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.infra.getMotionViews
import com.tejpratapsingh.motion.sdui.infra.toJson
import com.tejpratapsingh.motioneditor.TimelineItem
import com.tejpratapsingh.motioneditor.TimelineTrack
import com.tejpratapsingh.motionlib.core.MotionView

object TimelineUtils {
    fun fromSdui(
        context: Context,
        sduiJson: JsonObject,
    ): List<TimelineTrack> {
        val viewsJson = if (sduiJson.has("views") && sduiJson.get("views").isJsonArray) {
            sduiJson.get("views").asJsonArray
        } else {
            null
        }
        val views = sduiJson.getMotionViews(context)
        return views.mapIndexed { index, view ->
            val viewJson = if (viewsJson != null && index < viewsJson.size() && viewsJson.get(index).isJsonObject) {
                viewsJson.get(index).asJsonObject
            } else {
                view.toJson()
            }
            TimelineTrack(
                id = "track_$index",
                items =
                    listOf(
                        TimelineItem(
                            id = "item_$index",
                            type = view.javaClass.simpleName,
                            startFrame = view.startFrame,
                            endFrame = view.endFrame,
                            label = view.javaClass.simpleName,
                            original = view,
                            sdui = viewJson,
                        ),
                    ),
            )
        }
    }

    fun fromMotionViews(views: List<MotionView>): List<TimelineTrack> {
        return views.mapIndexed { index, view ->
            TimelineTrack(
                id = "track_$index",
                items =
                    listOf(
                        TimelineItem(
                            id = "item_$index",
                            type = view.javaClass.simpleName,
                            startFrame = view.startFrame,
                            endFrame = view.endFrame,
                            label = view.javaClass.simpleName,
                            original = view,
                            sdui = view.toJson(),
                        ),
                    ),
            )
        }
    }
}
