package com.tejpratapsingh.lyricsmaker.presentation.view

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.lyricsmaker.data.lrc.LrcHelper
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView

class LyricsTextView(
    context: Context,
    val lyrics: List<SyncedLyricFrame>,
    startFrame: Int,
    endFrame: Int,
    textView: AppCompatTextView = AppCompatTextView(context),
    fontUrl: String? = null,
) : AbstractMotionTextView(
        context = context,
        text = lyrics.first().text,
        startFrame = startFrame,
        endFrame = endFrame,
        textView = textView,
        fontUrl = fontUrl,
    ) {
    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)

        val currentLyric = LrcHelper.getCurrentLyric(lyrics = lyrics, currentFrame = frame)
        textView.text = currentLyric?.text ?: ""

        return this
    }
}
