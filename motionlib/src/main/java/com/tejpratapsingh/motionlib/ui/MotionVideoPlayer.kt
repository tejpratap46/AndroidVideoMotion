package com.tejpratapsingh.motionlib.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.widget.LinearLayoutCompat
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.core.MotionVideo
import com.tejpratapsingh.motionlib.utils.Utilities

class MotionVideoPlayer(context: Context, motionVideo: MotionVideo) :
    ContourLayout(context) {

    private val TAG = "MotionVideoPlayer"

    val seekBar: SeekBar = SeekBar(context).apply {
        max = motionVideo.totalFrames

        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                motionVideo.motionComposerView.forFrame(p1)

                imagePreview.setImageBitmap(Utilities.getViewBitmap(motionVideo.motionComposerView))
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    val controlsLayout: LinearLayoutCompat = LinearLayoutCompat(context).apply {
        orientation = LinearLayoutCompat.VERTICAL
        setBackgroundColor(Color.RED)
    }

    val previewLayout: LinearLayoutCompat = LinearLayoutCompat(context).apply {
        orientation = LinearLayoutCompat.VERTICAL
        setBackgroundColor(Color.GREEN)
    }

    val imagePreview: ImageView = ImageView(context)

    init {
        controlsLayout.addView(seekBar)

        controlsLayout.layoutBy(
            x = leftTo {
                parent.left()
            }.rightTo {
                parent.right()
            },
            y = bottomTo {
                parent.bottom()
            }
        )

        previewLayout.addView(imagePreview)
        previewLayout.layoutBy(
            x = leftTo {
                parent.left()
            }.rightTo {
                parent.right()
            },
            y = topTo {
                parent.top()
            }.bottomTo {
                controlsLayout.top()
            }
        )
        previewLayout.gravity = Gravity.CENTER_VERTICAL
    }
}