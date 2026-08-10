package com.tejpratapsingh.lyricsmaker.domain.sdui

import com.tejpratapsingh.lyricsmaker.presentation.view.MultiLyricsContainer
import com.tejpratapsingh.motion.sdui.infra.MotionSdui
import com.tejpratapsingh.motion.sdui.infra.parseMotionViewProps
import com.tejpratapsingh.motion.sdui.infra.toJson
import com.tejpratapsingh.motion.sdui.infra.toMotionAsset

object MultiLyricsContainerSdui {
    fun register() {
        // Register MultiLyricsContainer
        MotionSdui.registerView(MultiLyricsContainer::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val songName = json.get("songName")?.asString ?: ""
            val asset = json.get("asset")?.asJsonObject?.toMotionAsset(context)
            MultiLyricsContainer(
                context = context,
                songName = songName,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                asset = asset,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(MultiLyricsContainer::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("songName", view.songName)
            view.asset?.let { json.add("asset", it.toJson()) }
        }
    }
}
