package com.tejpratapsingh.motionlib.templates.extensions

import android.net.Uri
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope
import com.tejpratapsingh.motionlib.ui.custom.image.CircularMotionImageView
import com.tejpratapsingh.motionlib.ui.custom.image.MotionImageView

fun ContentScope.motionImageView(
    imageUri: Uri,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (MotionImageView.() -> Unit)? = null,
) = MotionImageView(context, imageUri, startFrame, endFrame, effects = effects)
    .apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }

fun ContentScope.circularMotionImageView(
    imageUri: Uri,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (CircularMotionImageView.() -> Unit)? = null,
) = CircularMotionImageView(context, imageUri, startFrame, endFrame, effects = effects)
    .apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }
