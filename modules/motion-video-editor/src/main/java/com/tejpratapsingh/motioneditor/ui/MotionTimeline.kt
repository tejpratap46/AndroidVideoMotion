package com.tejpratapsingh.motioneditor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.motioneditor.TimelineItem
import com.tejpratapsingh.motioneditor.TimelineTrack
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
@Suppress("ktlint:standard:function-naming")
fun MotionTimeline(
    tracks: List<TimelineTrack>,
    currentFrame: Int,
    totalFrames: Int,
    onFrameChange: (Int) -> Unit,
    onResize: (Float) -> Unit = {},
    fps: Int = 30,
    pixelsPerFrame: Float = 5f,
    modifier: Modifier = Modifier,
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    var lastFeedbackTime by remember { mutableLongStateOf(0L) }

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
    ) {
        val viewportWidth = constraints.maxWidth
        val halfWidth = with(density) { (viewportWidth / 2).toDp() }

        // Sync scroll position with currentFrame (when not scrolling manually)
        LaunchedEffect(currentFrame, pixelsPerFrame) {
            if (!horizontalScrollState.isScrollInProgress) {
                val currentX = (currentFrame * pixelsPerFrame * density.density)
                horizontalScrollState.scrollTo(currentX.toInt())
            }
        }

        // Sync currentFrame with scroll position (when scrolling manually)
        LaunchedEffect(horizontalScrollState, pixelsPerFrame, density, totalFrames, onFrameChange) {
            snapshotFlow { horizontalScrollState.value }
                .map { (it / (pixelsPerFrame * density.density)).toInt().coerceIn(0, totalFrames) }
                .distinctUntilChanged()
                .collectLatest { newFrame ->
                    if (horizontalScrollState.isScrollInProgress) {
                        onFrameChange(newFrame)

                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastFeedbackTime >= 50L) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                            lastFeedbackTime = currentTime
                        }
                    }
                }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Time Scale (Ruler) - Scrolls horizontally with tracks but fixed at top
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .pointerInput(onResize) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                onResize(dragAmount)
                            }
                        }.horizontalScroll(horizontalScrollState),
            ) {
                Row {
                    Spacer(modifier = Modifier.width(halfWidth))
                    TimeScaleView(
                        totalFrames = totalFrames,
                        fps = fps,
                        pixelsPerFrame = pixelsPerFrame,
                    )
                    Spacer(modifier = Modifier.width(halfWidth))
                }
            }

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .horizontalScroll(horizontalScrollState)
                            .verticalScroll(verticalScrollState)
                            .padding(vertical = 16.dp),
                ) {
                    tracks.forEach { track ->
                        Row {
                            Spacer(modifier = Modifier.width(halfWidth))
                            TimelineTrackView(track, pixelsPerFrame, fps)
                            Spacer(modifier = Modifier.width(halfWidth))
                        }
                    }
                }

                // Fixed Progress Marker (Always in the middle)
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(Color.Red),
                )
            }
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun TimeScaleView(
    totalFrames: Int,
    fps: Int,
    pixelsPerFrame: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val totalWidth = (totalFrames * pixelsPerFrame).dp

    Box(
        modifier =
            modifier
                .width(totalWidth)
                .height(40.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1.dp.toPx()
            val majorTickHeight = 15.dp.toPx()
            val minorTickHeight = 8.dp.toPx()

            for (frame in 0..totalFrames) {
                val x = frame * pixelsPerFrame * density.density
                val isMajorTick = frame % fps == 0
                val tickHeight = if (isMajorTick) majorTickHeight else minorTickHeight
                val color = if (isMajorTick) Color.Gray else Color.LightGray

                drawLine(
                    color = color,
                    start = Offset(x, size.height),
                    end = Offset(x, size.height - tickHeight),
                    strokeWidth = strokeWidth,
                )
            }
        }

        // Labels for major ticks
        for (frame in 0..totalFrames step fps) {
            val x = (frame * pixelsPerFrame).dp
            Text(
                text = formatFrameToTime(frame, fps),
                modifier =
                    Modifier
                        .offset(x = x + 4.dp, y = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatFrameToTime(
    frame: Int,
    fps: Int,
): String {
    val totalSeconds = frame / fps
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun TimelineTrackView(
    track: TimelineTrack,
    pixelsPerFrame: Float,
    fps: Int,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(vertical = 4.dp)
                .background(Color.DarkGray.copy(alpha = 0.1f)),
    ) {
        track.items.forEach { item ->
            TimelineItemView(item, pixelsPerFrame, fps)
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun TimelineItemView(
    item: TimelineItem,
    pixelsPerFrame: Float,
    fps: Int,
) {
    val startPx = (item.startFrame * pixelsPerFrame).dp
    val widthPx = ((item.endFrame - item.startFrame) * pixelsPerFrame).dp

    Box(
        modifier =
            Modifier
                .offset(x = startPx)
                .width(widthPx)
                .height(52.dp)
                .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                .padding(horizontal = 8.dp),
        contentAlignment = androidx.compose.ui.Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // "Icon" - First character
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                modifier = Modifier.size(28.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text =
                            item.label
                                .firstOrNull()
                                ?.toString()
                                ?.uppercase() ?: "?",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = item.label,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${formatFrameToTime(item.startFrame, fps)} - ${formatFrameToTime(item.endFrame, fps)}",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("ktlint:standard:function-naming")
fun PreviewMotionTimeline() {
    val sampleTracks =
        listOf(
            TimelineTrack(
                id = "1",
                items =
                    listOf(
                        TimelineItem("1", "Image", 0, 50, "Background Image"),
                    ),
            ),
            TimelineTrack(
                id = "2",
                items =
                    listOf(
                        TimelineItem("2", "Text", 10, 40, "Hello World"),
                    ),
            ),
            TimelineTrack(
                id = "3",
                items =
                    listOf(
                        TimelineItem("3", "Effect", 20, 60, "Fade In"),
                    ),
            ),
        )

    MaterialTheme {
        MotionTimeline(
            tracks = sampleTracks,
            currentFrame = 25,
            totalFrames = 100,
            fps = 30,
            onFrameChange = {},
        )
    }
}
