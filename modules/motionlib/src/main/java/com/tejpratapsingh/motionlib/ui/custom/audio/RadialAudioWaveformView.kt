package com.tejpratapsingh.motionlib.ui.custom.audio

import android.content.Context
import android.graphics.Canvas
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class RadialAudioWaveformView(
    context: Context,
    val amplitudes: List<Float> = emptyList(),
    override val startFrame: Int,
    override val endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
) : BaseAudioWaveformView(context, startFrame, endFrame, effects = effects) {
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (amplitudes.isEmpty() || startFrame >= endFrame) return

        val cx = width / 2f
        val cy = height / 2f
        val minRadius = min(cx, cy) * 0.5f
        val maxSpike = minRadius * 0.4f

        val segment = amplitudes.subList(startFrame, endFrame)
        val count = segment.size
        val angleStep = (2 * PI / count).toFloat()

        for (i in segment.indices) {
            val amp = segment[i].absoluteValue
            // Compute pulsing factor based on current frame:
            val distFromCursor = abs((i + startFrame) - currentFrame).toFloat()
            val maxDist = (endFrame - startFrame).coerceAtLeast(1).toFloat()
            // Closer spikes are longer:
            val frameFactor = 1f - (distFromCursor / maxDist)
            val length = amp * maxSpike * frameFactor

            val angle = i * angleStep
            val sx = cx + cos(angle) * minRadius
            val sy = cy + sin(angle) * minRadius
            val ex = cx + cos(angle) * (minRadius + length)
            val ey = cy + sin(angle) * (minRadius + length)

            val paintToUse = if (i + startFrame == currentFrame) cursorPaint else paint
            canvas.drawLine(sx, sy, ex, ey, paintToUse)
        }
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        currentFrame = frame.coerceIn(startFrame, endFrame)
        invalidate()
        return this
    }
}
