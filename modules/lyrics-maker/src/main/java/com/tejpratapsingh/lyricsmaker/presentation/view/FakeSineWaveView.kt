package com.tejpratapsingh.lyricsmaker.presentation.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin

class FakeSineWaveView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
    ) : View(context, attrs, defStyle) {
        private val wavePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.MAGENTA
                strokeWidth = 6f
                style = Paint.Style.STROKE
            }
        private val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 32f
            }

        /** externally controlled frame */
        private var frame: Int = 0

        /** control wave properties */
        var amplitude: Float = 0.3f // relative height of wave
        var wavelength: Float = 150f // pixels per wave cycle
        var speedFactor: Float = 0.05f // animation speed

        fun setFrame(frameNumber: Int) {
            frame = frameNumber
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val w = width.toFloat()
            val h = height.toFloat()
            val centerY = h / 2f

            val pathPoints = mutableListOf<Float>()

            val phaseShift = frame * speedFactor

            // Generate sine wave points
            var x = 0f
            while (x <= w) {
                val angle = (x / wavelength * 2 * Math.PI + phaseShift).toFloat()
                val y = centerY + sin(angle) * (h * amplitude)
                pathPoints.add(x)
                pathPoints.add(y)
                x += 4f // step size (smaller = smoother curve)
            }

            // Draw line segments between points
            for (i in 2 until pathPoints.size step 2) {
                val x1 = pathPoints[i - 2]
                val y1 = pathPoints[i - 1]
                val x2 = pathPoints[i]
                val y2 = pathPoints[i + 1]
                canvas.drawLine(x1, y1, x2, y2, wavePaint)
            }

            // Debug overlay
            canvas.drawText("Frame: $frame", 20f, 40f, textPaint)
        }
    }
