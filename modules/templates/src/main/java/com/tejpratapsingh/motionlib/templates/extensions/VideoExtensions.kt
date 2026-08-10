package com.tejpratapsingh.motionlib.templates.extensions

import android.net.Uri
import com.tejpratapsingh.motionlib.assettype.SimpleMotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope
import com.tejpratapsingh.motionlib.ui.custom.video.MediaFrameView
import com.tejpratapsingh.motionlib.ui.custom.video.VideoFrameView

fun ContentScope.videoFrameView(
    videoUri: Uri,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (VideoFrameView.() -> Unit)? = null,
) = VideoFrameView(context, SimpleMotionAsset(videoUri), startFrame, endFrame, effects = effects)
    .apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }

fun ContentScope.mediaFrameView(
    mediaUri: Uri,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (MediaFrameView.() -> Unit)? = null,
) = MediaFrameView(context, SimpleMotionAsset(mediaUri), startFrame, endFrame, effects = effects)
    .apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }
