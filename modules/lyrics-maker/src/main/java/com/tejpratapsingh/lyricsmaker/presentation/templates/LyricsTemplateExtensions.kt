package com.tejpratapsingh.lyricsmaker.presentation.templates

import android.net.Uri
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.view.IMessageChatMotionView
import com.tejpratapsingh.lyricsmaker.presentation.view.MultiLyricsContainer
import com.tejpratapsingh.motionlib.assettype.SimpleMotionAsset
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope

fun ContentScope.multiLyricsContainer(
    songName: String,
    startFrame: Int,
    endFrame: Int,
    image: String? = null,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (MultiLyricsContainer.() -> Unit)? = null,
) = MultiLyricsContainer(
    context,
    songName,
    startFrame,
    endFrame,
    asset = image?.let { SimpleMotionAsset(Uri.parse(it)) },
    effects = effects,
).apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }

fun ContentScope.iMessageChatMotionView(
    lyrics: List<SyncedLyricFrame>,
    startFrame: Int,
    endFrame: Int,
    bubbleColor: String = "#007AFF",
    textColor: String = "#FFFFFF",
    incomingBubbleColor: String = "#26252A",
    incomingTextColor: String = "#FFFFFF",
    writingSpeed: Float = 1.0f,
    unwrittenTextAlpha: Float = 0.0f,
    isAlternating: Boolean = false,
    fontAsset: MotionAsset? = null,
    textSizeVariant: MotionTextVariant = MotionTextVariant.H5,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (IMessageChatMotionView.() -> Unit)? = null,
) = IMessageChatMotionView(
    context = context,
    lyrics = lyrics,
    startFrame = startFrame,
    endFrame = endFrame,
    bubbleColor = bubbleColor,
    textColor = textColor,
    incomingBubbleColor = incomingBubbleColor,
    incomingTextColor = incomingTextColor,
    writingSpeed = writingSpeed,
    unwrittenTextAlpha = unwrittenTextAlpha,
    isAlternating = isAlternating,
    fontAsset = fontAsset,
    textSizeVariant = textSizeVariant,
    effects = effects,
).apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }

