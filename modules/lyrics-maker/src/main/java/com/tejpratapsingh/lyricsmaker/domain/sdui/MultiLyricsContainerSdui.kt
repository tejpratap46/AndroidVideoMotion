package com.tejpratapsingh.lyricsmaker.domain.sdui

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.view.MultiLyricsContainer
import com.tejpratapsingh.lyricsmaker.presentation.view.SyncedLyricsMotionTextView
import com.tejpratapsingh.motion.sdui.infra.MotionSdui
import com.tejpratapsingh.motion.sdui.infra.parseMotionViewProps
import com.tejpratapsingh.motion.sdui.infra.toJson
import com.tejpratapsingh.motion.sdui.infra.toMotionAsset
import com.tejpratapsingh.motionlib.core.MotionTextVariant

object MultiLyricsContainerSdui {
    fun register() {
        // Register SyncedLyricsMotionTextView
        MotionSdui.registerView(SyncedLyricsMotionTextView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val lyrics = json.get("lyrics")?.asJsonArray?.map {
                val lyricObject = it.asJsonObject
                SyncedLyricFrame(
                    frame = lyricObject.get("frame").asInt,
                    text = lyricObject.get("text").asString
                )
            } ?: emptyList()
            val fontAsset = json.get("fontAsset")?.asJsonObject?.toMotionAsset(context)
            val textSizeVariant = json.get("textSizeVariant")?.asString?.let { MotionTextVariant.valueOf(it) }
            val textColor = json.get("textColor")?.asString
            SyncedLyricsMotionTextView(
                context = context,
                lyrics = lyrics,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                fontAsset = fontAsset,
                textSizeVariant = textSizeVariant,
                textColor = textColor,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(SyncedLyricsMotionTextView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            val lyricsArray = JsonArray()
            view.lyrics.forEach { lyric ->
                val lyricObject = JsonObject()
                lyricObject.addProperty("frame", lyric.frame)
                lyricObject.addProperty("text", lyric.text)
                lyricsArray.add(lyricObject)
            }
            json.add("lyrics", lyricsArray)
            view.fontAsset?.let { json.add("fontAsset", it.toJson()) }
            view.textSizeVariant?.let { json.addProperty("textSizeVariant", it.name) }
            view.textColor?.let { json.addProperty("textColor", it) }
        }

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
