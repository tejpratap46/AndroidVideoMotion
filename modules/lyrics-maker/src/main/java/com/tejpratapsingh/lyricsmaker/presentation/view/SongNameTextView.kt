package com.tejpratapsingh.lyricsmaker.presentation.view

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView

class SongNameTextView(
    context: Context,
    val songName: String,
    startFrame: Int,
    endFrame: Int,
    textView: AppCompatTextView = AppCompatTextView(context),
    fontUrl: String? = null,
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
) : AbstractMotionTextView(
        context = context,
        text = songName,
        startFrame = startFrame,
        endFrame = endFrame,
        textView = textView,
        fontUrl = fontUrl,
        textSizeVariant = textSizeVariant,
        textColor = textColor,
        effects = effects,
    ) {
    init {
        textView.text = songName
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        return this
    }
}
