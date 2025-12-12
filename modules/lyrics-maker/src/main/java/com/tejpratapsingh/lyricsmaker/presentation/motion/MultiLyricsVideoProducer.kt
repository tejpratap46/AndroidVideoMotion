package com.tejpratapsingh.lyricsmaker.presentation.motion

import android.content.Context
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.ffmpeg.FfmpegVideoProducerAdapter
import com.tejpratapsingh.motionlib.ui.custom.text.WordBlinkTextView

fun getMultiLyricsVideoProducer(
    applicationContext: Context,
    song: String,
    lyrics: List<SyncedLyricFrame>,
): MotionVideoProducer {
    Log.d("MotionVideoProducer", "getMultiLyricsVideoProducer: ${lyrics.size}")

    val motionConfig =
        MotionConfig(
            aspectRatio = VideoAspectRatio.Ratio9x16_480,
            fps = 24,
        )

    val producer =
        MotionVideoProducer.with(
            context = applicationContext,
            config = motionConfig,
            videoProducerAdapter = FfmpegVideoProducerAdapter(),
        )

    lyrics.zipWithNext().forEach { (current, next) ->
        producer.addMotionViewToSequence(
            WordBlinkTextView(
                context = applicationContext,
                text = current.text,
                startFrame = current.frame,
                endFrame = next.frame,
                textView =
                    AppCompatTextView(applicationContext).apply {
                        textSize = 24f
                        setTextColor(android.graphics.Color.WHITE)
                        setPadding(16, 16, 16, 16)
                        textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                    },
            ),
        )
    }

    return producer
}
