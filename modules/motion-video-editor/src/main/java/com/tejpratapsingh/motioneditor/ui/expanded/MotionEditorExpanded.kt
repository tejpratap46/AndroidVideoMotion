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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.google.gson.JsonObject
import com.tejpratapsingh.motioneditor.TimelineItem
import com.tejpratapsingh.motioneditor.TimelineTrack
import com.tejpratapsingh.motioneditor.ui.MotionTimeline
import com.tejpratapsingh.motioneditor.ui.editor.MotionViewPropertyEditor
import com.tejpratapsingh.motionlib.core.findConfig
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.ui.custom.video.MotionVideoPlayerCompose
import com.tejpratapsingh.motionstore.tables.MotionProject

@Composable
fun MotionEditorExpanded(
    project: MotionProject,
    draftSdui: JsonObject,
    motionVideoProducer: MotionVideoProducer,
    timelineTracks: List<TimelineTrack>,
    currentFrame: Int,
    onFrameChange: (Int) -> Unit,
    timelineHeight: Dp,
    onTimelineHeightChange: (Dp) -> Unit,
    minTimelineHeight: Dp,
    maxTimelineHeight: Dp,
    selectedItem: TimelineItem?,
    selectedViewJson: JsonObject?,
    onItemSelect: (TimelineItem) -> Unit,
    onViewJsonChange: (JsonObject) -> Unit,
    onDismissEditor: () -> Unit,
    onSaveDraft: (MotionProject) -> Unit,
    onNavigateToAssetDownload: (String) -> Unit,
    onCheckPendingDownloads: (String) -> Boolean,
    undoRedoContent: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
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
                modifier = Modifier.width(240.dp).fillMaxHeight(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 1.dp,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "Layers",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            VerticalDivider()

            // Video Player (Main Area)
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                MotionVideoPlayerCompose(
                    motionVideoProducer = motionVideoProducer,
                    currentFrame = currentFrame,
                    onFrameChange = onFrameChange,
                    onBeforePlay = {
                        val hasPending = onCheckPendingDownloads(draftSdui.toString())
                        if (hasPending) {
                            onSaveDraft(project.copy(sdui = draftSdui))
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
                modifier = Modifier.width(340.dp).fillMaxHeight(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 1.dp,
            ) {
                if (selectedViewJson != null) {
                    MotionViewPropertyEditor(
                        viewJson = selectedViewJson,
                        onViewJsonChange = onViewJsonChange,
                        onClose = onDismissEditor,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(24.dp),
                    ) {
                        Text(
                            text = "Select an item in timeline to edit properties",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Undo/Redo Toolbar above timeline
        undoRedoContent()

        // Draggable Handle
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .pointerInput(density) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val dragAmountDp = with(density) { dragAmount.y.toDp() }
                            onTimelineHeightChange(
                                (currentTimelineHeight - dragAmountDp).coerceIn(
                                    currentMinHeight,
                                    currentMaxHeight,
                                ),
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
                    ),
                )
            },
            selectedItemId = selectedItem?.id,
            onItemClick = onItemSelect,
            fps = motionVideoProducer.motionComposerView.findConfig().fps,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(timelineHeight),
        )
    }
}
