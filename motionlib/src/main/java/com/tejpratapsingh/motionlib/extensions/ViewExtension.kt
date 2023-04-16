package com.tejpratapsingh.motionlib.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View

fun View.getViewBitmap(): Bitmap {
    //Get the dimensions of the view so we can re-layout the view at its current size
    //and create a bitmap of the same size
    val width = this.width
    val height = this.height
    val measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
    val measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

    //Cause the view to re-layout
    this.measure(measuredWidth, measuredHeight)
    this.layout(0, 0, this.measuredWidth, this.measuredHeight)

    //Create a bitmap backed Canvas to draw the view into
    val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val c = Canvas(b)

    //Now that the view is laid out and we have a canvas, ask the view to draw itself into the canvas
    this.draw(c)
    return b
}