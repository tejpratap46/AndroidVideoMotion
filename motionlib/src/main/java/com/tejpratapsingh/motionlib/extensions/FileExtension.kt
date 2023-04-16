package com.tejpratapsingh.motionlib.extensions

import android.graphics.Bitmap
import org.jcodec.api.FrameGrab
import org.jcodec.api.android.AndroidFrameGrab
import org.jcodec.common.AndroidUtil
import org.jcodec.common.DemuxerTrack
import org.jcodec.common.DemuxerTrackMeta
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Picture
import java.io.File

fun File.getVideoMetadata(): DemuxerTrackMeta {
    val vt: DemuxerTrack =
        AndroidFrameGrab.createFrameGrab(NIOUtils.readableChannel(this)).videoTrack
    return vt.meta
}

fun File.getSingleFrameFromVideo(frameNumber: Int): Bitmap {
    return AndroidFrameGrab.getFrame(this, frameNumber)
}

/**
 * Should not use this, it will require a lot of memory
 */
fun File.getAllFramesFromFile(): MutableList<Bitmap> {
    val bitmapList = mutableListOf<Bitmap>()
    val grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(this))
    var picture: Picture
    while (null != grab.nativeFrame.also { picture = it }) {
        bitmapList.add(AndroidUtil.toBitmap(picture))
    }
    return bitmapList
}