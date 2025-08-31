package com.tejpratapsingh.lyricsmaker.presentation.view

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.lyricsmaker.data.lrc.LrcHelper
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView

class LyricsTextView(
    context: Context,
    lyrics: String,
    startFrame: Int,
    endFrame: Int,
    textView: AppCompatTextView = AppCompatTextView(context),
    fontUrl: String? = null
) : AbstractMotionTextView(context, lyrics, startFrame, endFrame, textView, fontUrl) {

    val api = LrcHelper()
    val lyrics = api.getSyncedLyrics(lyrics, 30)

    override fun forFrame(frame: Int): MotionView {

        val currentLyric = api.getCurrentLyric(lyrics, frame)
        textView.text = currentLyric?.text ?: ""

        return this
    }
}