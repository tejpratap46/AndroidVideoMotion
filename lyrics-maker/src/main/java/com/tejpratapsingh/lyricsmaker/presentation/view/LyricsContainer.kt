package com.tejpratapsingh.lyricsmaker.presentation.view

import android.content.Context
import android.graphics.Bitmap
import android.view.Gravity
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.extensions.toBitmap
import com.tejpratapsingh.motionlib.core.motion.BaseMotionView

class LyricsContainer(
    context: Context, songName: String, lyrics: String, startFrame: Int, endFrame: Int
) : BaseMotionView(context, startFrame, endFrame) {

    private val songNameTextView: SongNameTextView = SongNameTextView(
        context = context, songName = songName, startFrame = startFrame, endFrame = endFrame
    ).apply {
        textView.textSize = 18f
        textView.gravity = Gravity.CENTER
    }

    private val lyricsTextView: LyricsTextView = LyricsTextView(
        context = context, lyrics = lyrics, startFrame = startFrame, endFrame = endFrame
    ).apply {
        textView.textSize = 18f
        textView.gravity = Gravity.CENTER
    }

    init {
        songNameTextView.layoutBy(x = leftTo {
            parent.left()
        }.rightTo {
            parent.right()
        }, y = topTo {
            parent.top()
        })

        lyricsTextView.layoutBy(x = leftTo {
            parent.left()
        }.rightTo {
            parent.right()
        }, y = topTo {
            songNameTextView.top()
        }.bottomTo {
            parent.bottom()
        })

        contourHeightOf {
            motionConfig.height.toYInt()
        }
        contourWidthOf {
            motionConfig.width.toXInt()
        }
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)

        val backgroundColor: Int = MotionInterpolator.interpolateColorForRange(
            Interpolators(Easings.LINEAR),
            frame,
            Pair(startFrame, endFrame),
            Pair("#2568ff".toColorInt(), "#ba28ff".toColorInt())
        )

//        setBackgroundColor(backgroundColor)

        songNameTextView.textView.setTextColor(
            MotionInterpolator.getComplementaryColor(
                backgroundColor
            )
        )

        lyricsTextView.textView.setTextColor(
            MotionInterpolator.getComplementaryColor(
                backgroundColor
            )
        )

        return this
    }

    override fun getViewBitmap(): Bitmap = this.toBitmap()
}