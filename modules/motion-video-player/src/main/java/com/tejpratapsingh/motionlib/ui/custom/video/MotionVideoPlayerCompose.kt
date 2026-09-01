package com.tejpratapsingh.motionlib.ui.custom.video

import android.graphics.Bitmap
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.findConfig
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@Composable
@Suppress("FunctionName")
fun MotionVideoPlayerCompose(
    motionVideoProducer: MotionVideoProducer,
    modifier: Modifier = Modifier,
    currentFrame: Int? = null,
    isPlaying: Boolean? = null,
    showControls: Boolean = true,
    onFrameChange: (Int) -> Unit = {},
    onPlayingChange: (Boolean) -> Unit = {},
    onBeforePlay: () -> Boolean = { true },
) {
    val context = LocalContext.current
    val motionConfig: MotionConfig = remember(motionVideoProducer) {
        motionVideoProducer.motionComposerView.findConfig()
    }
    val totalFrames = motionVideoProducer.totalFrames

    var internalCurrentFrame by remember { mutableIntStateOf(0) }
    val effectiveCurrentFrame = currentFrame ?: internalCurrentFrame

    var internalIsPlaying by remember { mutableStateOf(false) }
    val effectiveIsPlaying = isPlaying ?: internalIsPlaying

    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val activePlayers = remember { mutableStateMapOf<MotionAudio, MediaPlayer>() }

    // Update preview when effectiveCurrentFrame changes
    LaunchedEffect(motionVideoProducer, effectiveCurrentFrame) {
        motionVideoProducer.motionComposerView.forFrame(effectiveCurrentFrame)
        previewBitmap = motionVideoProducer.motionComposerView.getViewBitmap()
    }

    // Playback loop
    LaunchedEffect(effectiveIsPlaying, motionVideoProducer, totalFrames, motionConfig, onFrameChange, currentFrame) {
        if (effectiveIsPlaying) {
            val frameDurationMs = (1000.0 / motionConfig.fps).toLong()
            var lastFrameTime = System.currentTimeMillis()
            var currentFrameTracker = effectiveCurrentFrame

            while (effectiveIsPlaying) {
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - lastFrameTime

                if (elapsed >= frameDurationMs) {
                    val framesToAdvance = (elapsed / frameDurationMs).toInt().coerceAtLeast(1)
                    currentFrameTracker += framesToAdvance

                    if (currentFrameTracker <= totalFrames) {
                        if (currentFrame != null) {
                            onFrameChange(currentFrameTracker)
                        } else {
                            internalCurrentFrame = currentFrameTracker
                        }
                    } else {
                        // Loop
                        currentFrameTracker = 0
                        if (currentFrame != null) {
                            onFrameChange(0)
                        } else {
                            internalCurrentFrame = 0
                        }
                        activePlayers.values.forEach {
                            it.stop()
                            it.release()
                        }
                        activePlayers.clear()
                    }
                    lastFrameTime = currentTime
                }
                delay(10.milliseconds) // Smooth check
            }
        }
    }

    // Audio management
    LaunchedEffect(effectiveCurrentFrame, effectiveIsPlaying, motionVideoProducer) {
        if (effectiveIsPlaying) {
            motionVideoProducer.motionAudio.forEach { audio ->
                val shouldPlay = effectiveCurrentFrame in audio.delayFrame..audio.endFrame
                val player = activePlayers[audio]

                if (shouldPlay) {
                    if (player == null) {
                        val mediaPlayer =
                            MediaPlayer().apply {
                                setDataSource(context, audio.audioUri)
                                prepare()
                                start()
                            }
                        activePlayers[audio] = mediaPlayer
                    } else if (!player.isPlaying) {
                        player.start()
                    }
                } else if (player != null) {
                    if (player.isPlaying) player.pause()
                    player.release()
                    activePlayers.remove(audio)
                }
            }
        } else {
            activePlayers.values.forEach { if (it.isPlaying) it.pause() }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isPlaying == null) {
                internalIsPlaying = false
            }
            activePlayers.values.forEach {
                if (it.isPlaying) it.stop()
                it.release()
            }
            activePlayers.clear()
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Preview Area
        BoxWithConstraints(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(if (showControls) 16.dp else 0.dp),
            contentAlignment = Alignment.Center,
        ) {
            val aspectRatioValue = motionConfig.aspectRatio.width.toFloat() / motionConfig.aspectRatio.height.toFloat()

            previewBitmap?.let { bitmap ->
                Surface(
                    modifier =
                        Modifier
                            .sizeIn(maxWidth = 1200.dp, maxHeight = 800.dp)
                            .aspectRatio(aspectRatioValue)
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                            ),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black,
                    shadowElevation = 8.dp,
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Video Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }

        // Controls Area
        if (showControls) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .padding(16.dp),
            ) {
                Slider(
                    value = effectiveCurrentFrame.toFloat(),
                    onValueChange = {
                        if (currentFrame != null) {
                            onFrameChange(it.toInt())
                        } else {
                            internalCurrentFrame = it.toInt()
                        }
                        if (isPlaying == null) {
                            internalIsPlaying = false
                            onPlayingChange(false)
                        } else {
                            onPlayingChange(false)
                        }
                    },
                    valueRange = 0f..totalFrames.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        IconButton(onClick = {
                            val nextFrame = (effectiveCurrentFrame - 1).coerceAtLeast(0)
                            if (currentFrame != null) {
                                onFrameChange(nextFrame)
                            } else {
                                internalCurrentFrame = nextFrame
                            }
                            if (isPlaying == null) {
                                internalIsPlaying = false
                                onPlayingChange(false)
                            } else {
                                onPlayingChange(false)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Frame",
                            )
                        }

                        IconButton(
                            onClick = {
                                val nextPlayingState = if (isPlaying == null) !internalIsPlaying else !isPlaying
                                if (nextPlayingState) {
                                    if (onBeforePlay()) {
                                        if (isPlaying == null) {
                                            internalIsPlaying = true
                                            onPlayingChange(true)
                                        } else {
                                            onPlayingChange(true)
                                        }
                                    }
                                } else {
                                    if (isPlaying == null) {
                                        internalIsPlaying = false
                                        onPlayingChange(false)
                                    } else {
                                        onPlayingChange(false)
                                    }
                                }
                            },
                            modifier =
                                Modifier.background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp),
                                ),
                        ) {
                            Icon(
                                imageVector = if (effectiveIsPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (effectiveIsPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }

                        IconButton(onClick = {
                            val nextFrame = (effectiveCurrentFrame + 1).coerceAtMost(totalFrames)
                            if (currentFrame != null) {
                                onFrameChange(nextFrame)
                            } else {
                                internalCurrentFrame = nextFrame
                            }
                            if (isPlaying == null) {
                                internalIsPlaying = false
                                onPlayingChange(false)
                            } else {
                                onPlayingChange(false)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Frame",
                            )
                        }
                    }

                    Text(
                        text = "${formatTime(effectiveCurrentFrame, motionConfig.fps)} / ${formatTime(totalFrames, motionConfig.fps)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }
            }
        }
    }
}

private fun formatTime(
    frames: Int,
    fps: Int,
): String {
    val totalSeconds = frames / fps
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
