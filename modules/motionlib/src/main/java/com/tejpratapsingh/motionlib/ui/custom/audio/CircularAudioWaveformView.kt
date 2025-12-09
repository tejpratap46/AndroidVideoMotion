package com.tejpratapsingh.motionlib.ui.custom.audio

import android.content.Context
import android.graphics.Canvas
import com.tejpratapsingh.motionlib.core.MotionView
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CircularAudioWaveformView(
    context: Context,
    private val amplitudes: List<Float> = emptyList(),
    override var startFrame: Int,
    override var endFrame: Int
) : BaseAudioWaveformView(context, startFrame, endFrame) {

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (amplitudes.isEmpty() || startFrame >= endFrame) return

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) * 0.6f

        val segment = amplitudes.subList(startFrame, endFrame)
        val steps = segment.size
        val angleStep = (2 * Math.PI / steps).toFloat()

        // Draw spikes
        for (i in segment.indices) {
            val amp = segment[i].absoluteValue
            val spikeLength = amp * radius * 0.5f

            val angle = i * angleStep
            val startX = cx + cos(angle) * radius
            val startY = cy + sin(angle) * radius
            val stopX = cx + cos(angle) * (radius + spikeLength)
            val stopY = cy + sin(angle) * (radius + spikeLength)

            val paintToUse = if (i + startFrame == currentFrame) cursorPaint else paint
            canvas.drawLine(startX, startY, stopX, stopY, paintToUse)
        }
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        currentFrame = frame.coerceIn(startFrame, endFrame)
        invalidate()
        return this
    }
}