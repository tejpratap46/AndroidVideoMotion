package com.tejpratapsingh.lyricsmaker.presentation.view

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView

class SongNameTextView(
    context: Context,
    val songName: String,
    startFrame: Int,
    endFrame: Int,
    textView: AppCompatTextView = AppCompatTextView(context),
    fontUrl: String? = null,
) : AbstractMotionTextView(context, songName, startFrame, endFrame, textView, fontUrl) {
    init {
        textView.text = songName
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        return this
    }
}
