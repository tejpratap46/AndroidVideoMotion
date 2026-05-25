package com.tejpratapsingh.motioneditor.utils

import android.content.Context
import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.infra.getMotionViews
import com.tejpratapsingh.motioneditor.TimelineItem
import com.tejpratapsingh.motioneditor.TimelineTrack
import com.tejpratapsingh.motionlib.core.MotionView

object TimelineUtils {
    fun fromSdui(context: Context, sduiJson: JsonObject): List<TimelineTrack> {
        val views = sduiJson.getMotionViews(context)
        return fromMotionViews(views)
    }

    fun fromMotionViews(views: List<MotionView>): List<TimelineTrack> {
        // Simple mapping: each view gets its own track for now
        // A more advanced implementation might stack non-overlapping views on the same track
        return views.mapIndexed { index, view ->
            TimelineTrack(
                id = "track_$index",
                items = listOf(
                    TimelineItem(
                        id = view.hashCode().toString(),
                        type = view.javaClass.simpleName,
                        startFrame = view.startFrame,
                        endFrame = view.endFrame,
                        label = view.javaClass.simpleName
                    )
                )
            )
        }
    }
}
