package com.tejpratapsingh.ividemo.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withMatrix

class TrapezoidImageView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : View(context, attrs) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var bitmap: Bitmap? = null

        fun setImageBitmap(bmp: Bitmap) {
            bitmap = bmp
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val bmp = bitmap ?: return

            val w = bmp.width.toFloat()
            val h = bmp.height.toFloat()

            // Destination trapezoid (isosceles)
            val topInset = w * 0.3f // how much shorter the top is
            val src =
                floatArrayOf(
                    0f,
                    0f, // top-left
                    w,
                    0f, // top-right
                    0f,
                    h, // bottom-left
                    w,
                    h, // bottom-right
                )
            val dst =
                floatArrayOf(
                    topInset,
                    0f, // top-left
                    w - topInset,
                    0f, // top-right
                    0f,
                    h, // bottom-left
                    w,
                    h, // bottom-right
                )

            val matrix = Matrix()
            matrix.setPolyToPoly(src, 0, dst, 0, 4)
            canvas.withMatrix(matrix) {
                drawBitmap(bmp, 0f, 0f, paint)
            }
        }
    }
