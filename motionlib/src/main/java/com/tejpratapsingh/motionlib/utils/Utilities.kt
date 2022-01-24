package com.tejpratapsingh.motionlib.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import org.jcodec.api.FrameGrab
import org.jcodec.api.android.AndroidFrameGrab
import org.jcodec.common.AndroidUtil
import org.jcodec.common.DemuxerTrack
import org.jcodec.common.DemuxerTrackMeta
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Picture
import java.io.File


object Utilities {
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

    fun getVideoMetadata(file: File): DemuxerTrackMeta {
        val vt: DemuxerTrack =
            AndroidFrameGrab.createFrameGrab(NIOUtils.readableChannel(file)).videoTrack
        return vt.meta
    }

    fun getSingleFrameFromVideo(file: File, frameNumber: Int): Bitmap {
        return AndroidFrameGrab.getFrame(file, frameNumber)
    }

    /**
     * Should not use this, it will require a lot of memory
     */
    private fun getAllFramesFromFile(file: File): MutableList<Bitmap> {
        val bitmapList = mutableListOf<Bitmap>()
        val grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file))
        var picture: Picture
        while (null != grab.nativeFrame.also { picture = it }) {
            bitmapList.add(AndroidUtil.toBitmap(picture))
        }
        return bitmapList
    }
}