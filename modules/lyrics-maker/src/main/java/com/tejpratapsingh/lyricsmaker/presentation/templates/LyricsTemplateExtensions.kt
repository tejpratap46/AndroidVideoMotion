package com.tejpratapsingh.lyricsmaker.presentation.templates

import android.net.Uri
import com.tejpratapsingh.lyricsmaker.presentation.view.MultiLyricsContainer
import com.tejpratapsingh.motionlib.assettype.SimpleMotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
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
