package com.tejpratapsingh.motionlib.templates.extensions

import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope
import com.tejpratapsingh.motionlib.ui.custom.audio.CircularAudioWaveformView
import com.tejpratapsingh.motionlib.ui.custom.audio.RadialAudioWaveformView

fun ContentScope.circularAudioWaveformView(
    amplitudes: List<Float> = emptyList(),
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (CircularAudioWaveformView.() -> Unit)? = null,
) = CircularAudioWaveformView(context, amplitudes, startFrame, endFrame, effects = effects)
    .apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }

fun ContentScope.radialAudioWaveformView(
    amplitudes: List<Float> = emptyList(),
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (RadialAudioWaveformView.() -> Unit)? = null,
) = RadialAudioWaveformView(context, amplitudes, startFrame, endFrame, effects = effects)
    .apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }
