package com.tejpratapsingh.motioneditor.ui.expanded

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.motioneditor.TimelineTrack
import com.tejpratapsingh.motioneditor.ui.MotionTimeline
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionlib.ui.custom.video.MotionVideoPlayerCompose
import com.tejpratapsingh.motionstore.tables.MotionProject

@Composable
fun MotionEditorExpanded(
    project: MotionProject,
    motionVideoProducer: MotionVideoProducer,
    timelineTracks: List<TimelineTrack>,
    currentFrame: Int,
    onFrameChange: (Int) -> Unit,
    timelineHeight: Dp,
    onTimelineHeightChange: (Dp) -> Unit,
    minTimelineHeight: Dp,
    maxTimelineHeight: Dp,
    onNavigateToAssetDownload: (String) -> Unit,
    onCheckPendingDownloads: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val currentTimelineHeight by rememberUpdatedState(timelineHeight)
    val currentMinHeight by rememberUpdatedState(minTimelineHeight)
    val currentMaxHeight by rememberUpdatedState(maxTimelineHeight)

    Column(modifier = modifier.fillMaxSize()) {
        // Desktop Layout: Sidebar + Preview
        Row(modifier = Modifier.weight(1f)) {
            // Left Sidebar (Layers/Assets)
            Surface(
                modifier = Modifier.width(280.dp).fillMaxHeight(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 1.dp,
            ) {
                // Empty for now
            }

            VerticalDivider()

            // Video Player (Main Area)
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                MotionVideoPlayerCompose(
                    motionVideoProducer = motionVideoProducer,
                    currentFrame = currentFrame,
                    onFrameChange = onFrameChange,
                    onBeforePlay = {
                        val hasPending = onCheckPendingDownloads(project.sdui.toString())
                        if (hasPending) {
                            onNavigateToAssetDownload(project.id)
                            false
                        } else {
                            true
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            VerticalDivider()

            // Right Sidebar (Properties)
            Surface(
                modifier = Modifier.width(300.dp).fillMaxHeight(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 1.dp,
            ) {
                // Empty for now
            }
        }

        // Draggable Handle
        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(24.dp) // Increased hit area
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .pointerInput(density) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val dragAmountDp = with(density) { dragAmount.y.toDp() }
                        onTimelineHeightChange(
                            (currentTimelineHeight - dragAmountDp).coerceIn(
                                currentMinHeight,
                                currentMaxHeight,
                            )
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        shape = CircleShape,
                    ),
            )
        }

        // Timeline at the bottom
        MotionTimeline(
            tracks = timelineTracks,
            currentFrame = currentFrame,
            totalFrames = motionVideoProducer.totalFrames,
            onFrameChange = onFrameChange,
            onResize = { dragAmount ->
                val dragAmountDp = with(density) { dragAmount.toDp() }
                onTimelineHeightChange(
                    (timelineHeight - dragAmountDp).coerceIn(
                        minTimelineHeight,
                        maxTimelineHeight,
                    )
                )
            },
            fps = provideCurrentConfig().fps,
            modifier =
            Modifier
                .fillMaxWidth()
                .height(timelineHeight),
        )
    }
}
