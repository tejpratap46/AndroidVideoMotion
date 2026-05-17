package com.tejpratapsingh.motionlib.ui.custom.video

import android.graphics.Bitmap
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun MotionVideoPlayerCompose(
    motionVideoProducer: MotionVideoProducer,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val motionConfig: MotionConfig = remember { provideCurrentConfig() }
    val totalFrames = motionVideoProducer.totalFrames

    var currentFrame by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val activePlayers = remember { mutableStateMapOf<MotionAudio, MediaPlayer>() }

    // Update preview when currentFrame changes
    LaunchedEffect(currentFrame) {
        motionVideoProducer.motionComposerView.forFrame(currentFrame)
        previewBitmap = motionVideoProducer.motionComposerView.getViewBitmap()
    }

    // Playback loop
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val frameDurationMs = (1000.0 / motionConfig.fps).toLong()
            var lastFrameTime = System.currentTimeMillis()

            while (isPlaying) {
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - lastFrameTime

                if (elapsed >= frameDurationMs) {
                    val framesToAdvance = (elapsed / frameDurationMs).toInt().coerceAtLeast(1)
                    val nextFrame = currentFrame + framesToAdvance

                    if (nextFrame <= totalFrames) {
                        currentFrame = nextFrame
                    } else {
                        // Loop
                        currentFrame = 0
                        activePlayers.values.forEach { it.stop(); it.release() }
                        activePlayers.clear()
                    }
                    lastFrameTime = currentTime
                }
                delay(10) // Smooth check
            }
        }
    }

    // Audio management
    LaunchedEffect(currentFrame, isPlaying) {
        if (isPlaying) {
            motionVideoProducer.motionAudio.forEach { audio ->
                val shouldPlay = currentFrame in audio.delayFrame..audio.endFrame
                val player = activePlayers[audio]

                if (shouldPlay) {
                    if (player == null) {
                        val mediaPlayer = MediaPlayer().apply {
                            setDataSource(audio.file.absolutePath)
                            prepare()
                            start()
                        }
                        activePlayers[audio] = mediaPlayer
                    } else if (!player.isPlaying) {
                        player.start()
                    }
                } else {
                    if (player != null) {
                        if (player.isPlaying) player.pause()
                        player.release()
                        activePlayers.remove(audio)
                    }
                }
            }
        } else {
            activePlayers.values.forEach { if (it.isPlaying) it.pause() }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            isPlaying = false
            activePlayers.values.forEach {
                if (it.isPlaying) it.stop()
                it.release()
            }
            activePlayers.clear()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Preview Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            previewBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Video Preview",
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(
                            motionConfig.aspectRatio.width.toFloat() / motionConfig.aspectRatio.height.toFloat()
                        ),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Controls Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                .padding(16.dp)
        ) {
            Slider(
                value = currentFrame.toFloat(),
                onValueChange = {
                    currentFrame = it.toInt()
                    isPlaying = false // Pause on seek
                },
                valueRange = 0f..totalFrames.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { isPlaying = !isPlaying }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }

                Text(
                    text = "${formatTime(currentFrame, motionConfig.fps)} / ${formatTime(totalFrames, motionConfig.fps)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun formatTime(frames: Int, fps: Int): String {
    val totalSeconds = frames / fps
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
