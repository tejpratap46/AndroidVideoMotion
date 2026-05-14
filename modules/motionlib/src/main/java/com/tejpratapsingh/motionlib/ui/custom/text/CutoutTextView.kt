package com.tejpratapsingh.motionlib.ui.custom.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

internal class CutoutTextView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
    ) : AppCompatTextView(context, attrs, defStyle) {
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val porterDuffMode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        override fun onDraw(canvas: Canvas) {
            // Draw the background image first
            val bgDrawable = background
            bgDrawable?.setBounds(0, 0, width, height)
            bgDrawable?.draw(canvas)

            // Create a new layer for cutout
            val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

            // Fill with black overlay
            canvas.drawColor(Color.BLACK)

            // Cut out text
            textPaint.typeface = typeface
            textPaint.textSize = textSize
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.xfermode = porterDuffMode

            val xPos = width / 2f
            val yPos = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(text.toString(), xPos, yPos, textPaint)

            textPaint.xfermode = null
            canvas.restoreToCount(saveCount)
        }
    }
