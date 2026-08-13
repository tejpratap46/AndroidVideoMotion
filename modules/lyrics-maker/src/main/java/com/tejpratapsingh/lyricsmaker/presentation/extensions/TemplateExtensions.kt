package com.tejpratapsingh.lyricsmaker.presentation.extensions

import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.view.SyncedLyricsMotionTextView
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope

fun ContentScope.syncedLyricsMotionTextView(
    lyrics: List<SyncedLyricFrame>,
    startFrame: Int,
    endFrame: Int,
    fontAsset: MotionAsset? = null,
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (SyncedLyricsMotionTextView.() -> Unit)? = null,
) = SyncedLyricsMotionTextView(
    context = context,
    lyrics = lyrics,
    startFrame = startFrame,
    endFrame = endFrame,
    fontAsset = fontAsset,
    textSizeVariant = textSizeVariant,
    textColor = textColor,
    effects = effects,
).apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }
