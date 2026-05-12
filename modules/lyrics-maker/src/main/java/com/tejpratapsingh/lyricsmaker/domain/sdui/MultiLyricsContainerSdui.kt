package com.tejpratapsingh.lyricsmaker.domain.sdui

import com.tejpratapsingh.lyricsmaker.presentation.view.MultiLyricsContainer
import com.tejpratapsingh.motion.sdui.infra.MotionSdui
import com.tejpratapsingh.motion.sdui.infra.parseMotionViewProps

object MultiLyricsContainerSdui {
    fun register() {
        // Register MultiLyricsContainer
        MotionSdui.registerView(MultiLyricsContainer::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps()
            val songName = json.get("songName")?.asString ?: ""
            val image = json.get("image")?.asString
            MultiLyricsContainer(
                context = context,
                songName = songName,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                image = image,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(MultiLyricsContainer::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("songName", view.songName)
            json.addProperty("image", view.image)
        }
    }
}
