package com.tejpratapsingh.motionlib.ui

import android.content.Context
import android.graphics.Color
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.appcompat.widget.LinearLayoutCompat
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.core.MotionVideoProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MotionVideoPlayer(context: Context, private val motionVideoProducer: MotionVideoProducer) :
    ContourLayout(context) {

    companion object {
        private const val TAG = "MotionVideoPlayer"
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var playbackJob: Job? = null

    private val playbackDelayMs = 1000L / motionVideoProducer.motionConfig.fps

    private var isPlaying = false

    val seekBar: SeekBar = SeekBar(context).apply {
        max = motionVideoProducer.totalFrames

        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) { // Only update preview if change is from user interaction
                    motionVideoProducer.motionComposerView.forFrame(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Pause playback when user starts dragging
                if (isPlaying) {
                    pausePlayback()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Resume playback or leave it paused
            }
        })
    }

    private val playPauseButton: ImageButton = ImageButton(context).apply {
        setImageResource(android.R.drawable.ic_media_play)
        setOnClickListener {
            if (isPlaying) {
                pausePlayback()
            } else {
                startPlayback()
            }
        }
    }

    private val controlsLayout: LinearLayoutCompat = LinearLayoutCompat(context).apply {
        orientation = LinearLayoutCompat.HORIZONTAL
        setBackgroundColor(Color.RED) // Consider removing or styling this appropriately
        gravity = android.view.Gravity.CENTER_VERTICAL // Center items in controls
    }

    private val previewLayout: LinearLayoutCompat = LinearLayoutCompat(context).apply {
        orientation = LinearLayoutCompat.VERTICAL
        setBackgroundColor(Color.GREEN) // Consider removing or styling this appropriately
        gravity = android.view.Gravity.CENTER // Center preview
    }

    init {
        controlsLayout.addView(playPauseButton)
        controlsLayout.addView(seekBar)

        val seekBarParams = LinearLayoutCompat.LayoutParams(
            0,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        ).apply {
            weight = 1f
        }
        seekBar.layoutParams = seekBarParams

        controlsLayout.layoutBy(
            x = leftTo { parent.left() }.rightTo { parent.right() },
            y = bottomTo { parent.bottom() }
        )

        previewLayout.addView(motionVideoProducer.motionComposerView)
        previewLayout.layoutBy(
            x = leftTo { parent.left() }.rightTo { parent.right() },
            y = topTo { parent.top() }.bottomTo { controlsLayout.top() }
        )
    }

    private fun startPlayback() {
        if (isPlaying) return

        isPlaying = true
        playPauseButton.setImageResource(android.R.drawable.ic_media_pause)

        // Launch a new coroutine for playback
        playbackJob = scope.launch {
            while (isPlaying) {
                var currentProgress = seekBar.progress
                if (currentProgress < seekBar.max) {
                    currentProgress++
                    seekBar.progress = currentProgress
                    motionVideoProducer.motionComposerView.forFrame(currentProgress)
                    delay(playbackDelayMs)
                } else {
                    seekBar.progress = 0 // Optional: reset to beginning
                    motionVideoProducer.motionComposerView.forFrame(0)
                }
            }
        }
    }

    private fun pausePlayback() {
        if (!isPlaying && playbackJob == null) return // Already paused or never started

        isPlaying = false
        playPauseButton.setImageResource(android.R.drawable.ic_media_play)
        playbackJob?.cancel() // Cancel the running coroutine
        playbackJob = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pausePlayback()
    }
}
