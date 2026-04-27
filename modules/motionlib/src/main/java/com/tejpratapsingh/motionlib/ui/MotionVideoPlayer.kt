package com.tejpratapsingh.motionlib.ui

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.SystemClock
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MotionVideoPlayer(
    context: Context,
    private val motionVideoProducer: MotionVideoProducer,
) : ContourLayout(context) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var playbackJob: Job? = null

    private val motionConfig: MotionConfig = provideCurrentConfig()

    private var isPlaying = false

    private val activePlayers = mutableMapOf<MotionAudio, MediaPlayer>()

    val seekBar: SeekBar =
        SeekBar(context).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                min = 1
            } // Start from 1 to avoid confusion with frame 0
            max = motionVideoProducer.totalFrames

            setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean,
                    ) {
                        if (fromUser) { // Only update preview if change is from user interaction
                            motionVideoProducer.motionComposerView.forFrame(progress)
                        }
                        currentTimeTextView.text = formatTime(progress)
                        overlayImageView.setImageBitmap(
                            motionVideoProducer
                                .motionComposerView
                                .getViewBitmap(),
                        )
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
                },
            )
        }

    private val playPauseButton: ImageButton =
        ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_media_play)
            setOnClickListener {
                if (isPlaying) {
                    pausePlayback()
                } else {
                    startPlayback()
                }
            }
        }

    private val currentTimeTextView: TextView =
        TextView(context).apply {
            text = formatTime(0)
            setTextColor(Color.WHITE)
            setPadding(16, 0, 16, 0)
        }

    private val totalTimeTextView: TextView =
        TextView(context).apply {
            text = formatTime(motionVideoProducer.totalFrames)
            setTextColor(Color.WHITE)
            setPadding(16, 0, 16, 0)
        }

    private val controlsLayout: LinearLayoutCompat =
        LinearLayoutCompat(context).apply {
            orientation = LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL // Center items in controls
        }

    val overlayImageView: ImageView =
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_XY
        }

    private val previewLayout: FrameLayout =
        FrameLayout(context)

    init {
        controlsLayout.addView(playPauseButton)
        controlsLayout.addView(currentTimeTextView)
        controlsLayout.addView(seekBar)
        controlsLayout.addView(totalTimeTextView)

        val seekBarParams =
            LinearLayoutCompat
                .LayoutParams(
                    0,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                ).apply {
                    weight = 1f
                }
        seekBar.layoutParams = seekBarParams

        controlsLayout.layoutBy(
            x = leftTo { parent.left() }.rightTo { parent.right() },
            y = bottomTo { parent.bottom() },
        )

        val previewLayoutParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                android.view.Gravity.CENTER,
            )
        previewLayout.addView(motionVideoProducer.motionComposerView, previewLayoutParams)
        previewLayout.addView(overlayImageView, previewLayoutParams)

        overlayImageView.setBackgroundColor(Color.WHITE)

        previewLayout.layoutBy(
            x = leftTo { parent.left() }.rightTo { parent.right() },
            y = topTo { parent.top() }.bottomTo { controlsLayout.top() },
        )
    }

    private fun startAudioIfNeeded(
        frame: Int,
        motionAudios: List<MotionAudio>,
    ) {
        motionAudios.forEach { audio ->
            val shouldPlay = frame in audio.delayFrame..audio.endFrame
            val player = activePlayers[audio]

            if (shouldPlay) {
                if (player == null) {
                    // First time: create player
                    val mediaPlayer =
                        android.media.MediaPlayer().apply {
                            setDataSource(audio.file.absolutePath)
                            prepare()
                            start()
                        }
                    activePlayers[audio] = mediaPlayer
                } else if (!player.isPlaying) {
                    // Resume instead of restarting
                    player.start()
                }
            } else {
                // Outside of valid range → stop & release
                if (player != null) {
                    if (player.isPlaying) player.pause()
                    player.release()
                    activePlayers.remove(audio)
                }
            }
        }
    }

    private fun pauseAllAudio() {
        activePlayers.values.forEach { player ->
            if (player.isPlaying) {
                player.pause()
            }
        }
    }

    private fun stopAllAudio() {
        activePlayers.values.forEach { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }
        activePlayers.clear()
    }

    private fun startPlayback() {
        if (isPlaying) return

        isPlaying = true
        playPauseButton.setImageResource(android.R.drawable.ic_media_pause)

        val frameDurationMs = 1000.0 / motionConfig.fps

        // Launch a new coroutine for playback
        playbackJob =
            scope.launch {
                var startTime = SystemClock.elapsedRealtime()
                var startFrame = seekBar.progress

                while (isPlaying) {
                    val elapsed = SystemClock.elapsedRealtime() - startTime
                    val expectedFrame = startFrame + (elapsed / frameDurationMs).toInt()

                    if (expectedFrame <= seekBar.max) {
                        if (expectedFrame != seekBar.progress) {
                            motionVideoProducer.motionComposerView.forFrame(expectedFrame)

                            // 🔊 Check if we should play audio
                            startAudioIfNeeded(
                                frame = expectedFrame,
                                motionAudios = motionVideoProducer.motionAudio,
                            )

                            seekBar.progress = expectedFrame
                        }
                    } else {
                        // Loop
                        val resetFrame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) seekBar.min else 0
                        seekBar.progress = resetFrame
                        motionVideoProducer.motionComposerView.forFrame(resetFrame)
                        stopAllAudio()

                        // Reset timer for the next loop iteration
                        startTime = SystemClock.elapsedRealtime()
                        startFrame = resetFrame
                    }
                    delay(10) // Check frequently enough for smooth playback but avoid 100% CPU usage
                }
            }
    }

    private fun pausePlayback() {
        if (!isPlaying && playbackJob == null) return

        isPlaying = false
        playPauseButton.setImageResource(android.R.drawable.ic_media_play)
        playbackJob?.cancel()
        playbackJob = null
        pauseAllAudio() // ✅ pause, not release
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pausePlayback()
        scope.cancel()
        stopAllAudio()
    }

    private fun formatTime(frames: Int): String {
        val totalSeconds = frames / motionConfig.fps
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
