package com.tejpratapsingh.lyricsmaker.presentation.view

import android.content.Context
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.lyricsmaker.data.lrc.LrcHelper
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.ui.custom.text.abstract.AbstractMotionTextView

/**
 * A [MotionView] that displays lyrics synced with the video progress.
 */
class SyncedLyricsMotionTextView(
    context: Context,
    val lyrics: List<SyncedLyricFrame>,
    startFrame: Int,
    endFrame: Int,
    fontAsset: MotionAsset? = null,
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
) : AbstractMotionTextView(
    context = context,
    text = LrcHelper.getCurrentLyric(lyrics, startFrame)?.text ?: lyrics.firstOrNull()?.text ?: "",
    startFrame = startFrame,
    endFrame = endFrame,
    textView = AppCompatTextView(context).apply {
        gravity = Gravity.CENTER
    },
    fontAsset = fontAsset,
    textSizeVariant = textSizeVariant,
    textColor = textColor,
    effects = effects,
) {
    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        val currentLyric = LrcHelper.getCurrentLyric(lyrics = lyrics, currentFrame = frame)
            ?: lyrics.firstOrNull() // Fallback to first lyric for preview
        
        textView.text = currentLyric?.text ?: ""
        return this
    }
}
