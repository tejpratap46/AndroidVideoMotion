package com.tejpratapsingh.motionlib.ui

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.appcompat.widget.LinearLayoutCompat
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.core.MotionVideo

class MotionVideoPlayer(context: Context, private val motionVideo: MotionVideo) :
    ContourLayout(context) {

    companion object {
        private const val TAG = "MotionVideoPlayer"
        private const val PLAYBACK_DELAY_MS = 100L // Adjust for desired playback speed
    }

    private var isPlaying = false
    private val playbackHandler = Handler(Looper.getMainLooper())
    private var playbackRunnable: Runnable? = null

    val seekBar: SeekBar = SeekBar(context).apply {
        max = motionVideo.totalFrames

        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) { // Only update preview if change is from user interaction
                    motionVideo.motionComposerView.forFrame(progress)
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
        // Replace with your actual play/pause icons
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
        controlsLayout.addView(playPauseButton) // Add the button first
        controlsLayout.addView(seekBar) // Then the SeekBar

        // Adjust SeekBar layout params to take remaining space
        val seekBarParams = LinearLayoutCompat.LayoutParams(
            0, // width
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT // height
        ).apply {
            weight = 1f
        }
        seekBar.layoutParams = seekBarParams

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

        previewLayout.addView(motionVideo.motionComposerView)
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
        // previewLayout.gravity = Gravity.CENTER_VERTICAL // Already set in constructor
    }

    private fun startPlayback() {
        if (isPlaying) return

        isPlaying = true
        playPauseButton.setImageResource(android.R.drawable.ic_media_pause) // Change to pause icon

        playbackRunnable = object : Runnable {
            override fun run() {
                if (!isPlaying) return

                var currentProgress = seekBar.progress
                if (currentProgress < seekBar.max) {
                    currentProgress++
                    seekBar.progress = currentProgress
                    // Update image preview based on new progress
                    motionVideo.motionComposerView.forFrame(currentProgress)

                    playbackHandler.postDelayed(this, PLAYBACK_DELAY_MS)
                } else {
                    // Reached the end, pause and reset
                    pausePlayback()
                    seekBar.progress = 0 // Optional: reset to beginning
                    motionVideo.motionComposerView.forFrame(0)
                }
            }
        }
        playbackHandler.post(playbackRunnable!!)
    }

    private fun pausePlayback() {
        if (!isPlaying) return

        isPlaying = false
        playPauseButton.setImageResource(android.R.drawable.ic_media_play) // Change to play icon
        playbackRunnable?.let {
            playbackHandler.removeCallbacks(it)
        }
        playbackRunnable = null
    }

    // Call this method when the view is detached to prevent memory leaks
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pausePlayback() // Ensure playback stops
    }
}
