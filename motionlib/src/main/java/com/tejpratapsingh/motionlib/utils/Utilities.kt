package com.tejpratapsingh.motionlib.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View


class Utilities {

    companion object {
        fun loadBitmapFromView(v: View): Bitmap {
            val b = Bitmap.createBitmap(
                v.measuredWidth,
                v.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            val c = Canvas(b)
            v.layout(0, 0, v.measuredWidth, v.measuredHeight)
            v.draw(c)
            return b
        }

        fun getViewBitmap(view: View): Bitmap {
            //Get the dimensions of the view so we can re-layout the view at its current size
            //and create a bitmap of the same size
            val width = view.width
            val height = view.height
            val measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
            val measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

            //Cause the view to re-layout
            view.measure(measuredWidth, measuredHeight)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)

            //Create a bitmap backed Canvas to draw the view into
            val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)

            //Now that the view is laid out and we have a canvas, ask the view to draw itself into the canvas
            view.draw(c)
            return b
        }
    }

}