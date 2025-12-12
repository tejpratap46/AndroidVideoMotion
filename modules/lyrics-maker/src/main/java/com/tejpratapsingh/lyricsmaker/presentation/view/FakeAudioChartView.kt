package com.tejpratapsingh.lyricsmaker.presentation.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

class FakeAudioChartView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
    ) : View(context, attrs, defStyle) {
        private val barPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
        private val linePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 50
                strokeWidth = 2f
            }

        var bars: Int = 48
        var barWidthPx: Float = 20f
        var barSpacingPx: Float = 10f
        var seed: Long = 12345L

        /** Control how fast bars move (smaller = slower, larger = faster) */
        var speedFactor: Float = 0.05f // try 0.01f for very slow, 0.1f for fast

        private val rngs by lazy {
            List(bars) { Random(seed + it * 7919L) }
        }

        /** Current frame externally controlled */
        private var frame: Int = 0

        fun setFrame(frameNumber: Int) {
            frame = frameNumber
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val w = width.toFloat()
            val h = height.toFloat()
            val totalWidth = bars * barWidthPx + (bars - 1) * barSpacingPx
            val startX = (w - totalWidth) / 2f

            for (i in 0 until bars) {
                val r = rngs[i]

                // Make each bar evolve slower using speedFactor
                val phase = (frame * speedFactor + i * 0.3f) % 1f

                // Use seeded noise with smooth oscillation instead of raw random
                val base = 0.5f + 0.5f * sin((frame * speedFactor + i) * 2.0)
                val noise = r.nextFloat() * 0.2f
                val finalAmp = ((base + noise) * 0.9f).coerceIn(0.0, 1.0)

                val barHeight = (finalAmp * h).coerceAtLeast(4.0)
                val x = startX + i * (barWidthPx + barSpacingPx)
                val yTop: Float = (h - barHeight).toFloat() / 2f
                val yBottom: Float = yTop + barHeight.toFloat()

                canvas.drawRoundRect(
                    x,
                    yTop,
                    x + barWidthPx,
                    yBottom,
                    barWidthPx / 2f,
                    barWidthPx / 2f,
                    barPaint,
                )
                canvas.drawLine(
                    x + barWidthPx / 2f,
                    yTop,
                    x + barWidthPx / 2f,
                    yBottom,
                    linePaint,
                )
            }
        }
    }
