package com.tejpratapsingh.motionlib.ui.custom.video

import android.R
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.SystemClock
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.findMotionConfig
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

class MotionVideoPlayer(
    context: Context,
    private val motionVideoProducer: MotionVideoProducer,
) : ContourLayout(context) {
    var onBeforePlay: () -> Boolean = { true }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var playbackJob: Job? = null

    private val motionConfig: MotionConfig by lazy { findMotionConfig() }

    private var isPlaying = false

    private val activePlayers = mutableMapOf<MotionAudio, MediaPlayer>()

    val seekBar: SeekBar =
        SeekBar(context).apply {
            min = 1
            // Start from 1 to avoid confusion with frame 0
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
            setImageResource(R.drawable.ic_media_play)
            setOnClickListener {
                if (isPlaying) {
                    pausePlayback()
                } else {
                    if (onBeforePlay()) {
                        startPlayback()
                    }
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
            gravity = Gravity.CENTER_VERTICAL // Center items in controls
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
                Gravity.CENTER,
            )
        previewLayout.addView(motionVideoProducer.motionComposerView, previewLayoutParams)

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
                        MediaPlayer().apply {
                            setDataSource(context, audio.audioUri)
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
        playPauseButton.setImageResource(R.drawable.ic_media_pause)

        val frameDurationMs = 1000.0 / motionConfig.fps

        // Launch a new coroutine for playback
        playbackJob =
            scope.launch {
                var startTime = SystemClock.elapsedRealtime()
                var startFrameValue = seekBar.progress
                var lastRenderedFrame = startFrameValue

                while (isPlaying) {
                    val elapsed = SystemClock.elapsedRealtime() - startTime
                    val expectedFrame = startFrameValue + (elapsed / frameDurationMs).toInt()

                    if (expectedFrame <= seekBar.max) {
                        if (expectedFrame != lastRenderedFrame) {
                            motionVideoProducer.motionComposerView.forFrame(expectedFrame)

                            // 🔊 Check if we should play audio
                            startAudioIfNeeded(
                                frame = expectedFrame,
                                motionAudios = motionVideoProducer.motionAudio,
                            )

                            seekBar.progress = expectedFrame
                            lastRenderedFrame = expectedFrame
                        }
                    } else {
                        // Loop
                        val resetFrame = seekBar.min
                        seekBar.progress = resetFrame
                        motionVideoProducer.motionComposerView.forFrame(resetFrame)
                        stopAllAudio()

                        // Reset timer for the next loop iteration
                        startTime = SystemClock.elapsedRealtime()
                        startFrameValue = resetFrame
                        lastRenderedFrame = resetFrame
                    }
                    delay(10.milliseconds) // Check frequently enough for smooth playback but avoid 100% CPU usage
                }
            }
    }

    private fun pausePlayback() {
        if (!isPlaying && playbackJob == null) return

        isPlaying = false
        playPauseButton.setImageResource(R.drawable.ic_media_play)
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
