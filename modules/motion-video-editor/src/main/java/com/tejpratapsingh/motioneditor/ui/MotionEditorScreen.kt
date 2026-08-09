package com.tejpratapsingh.motioneditor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.infra.SDUIMotionVideoProducerFactory
import com.tejpratapsingh.motioneditor.utils.TimelineUtils
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import com.tejpratapsingh.motionlib.ui.custom.video.MotionVideoPlayerCompose
import com.tejpratapsingh.motionstore.tables.MotionProject
import com.tejpratapsingh.motionstore.tables.SyncTracker

@Composable
@Suppress("ktlint:standard:function-naming")
fun MotionEditorScreen(
    project: MotionProject,
    onBackClick: () -> Unit,
    onSaveClick: (MotionProject) -> Unit,
    onNavigateToAssetDownload: (String) -> Unit,
    onCheckPendingDownloads: (String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val producerFactory = remember { SDUIMotionVideoProducerFactory(context) }
    val motionVideoProducer =
        remember(project) {
            producerFactory.createFromProject(project)
        }
    val timelineTracks =
        remember(project) {
            TimelineUtils.fromSdui(context, project.sdui)
        }

    var currentFrame by remember { mutableIntStateOf(0) }
    var timelineHeight by remember { mutableStateOf(300.dp) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxHeight = maxHeight
        val minTimelineHeight = 150.dp
        val maxTimelineHeight = maxHeight - 150.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Video Player at the top
            MotionVideoPlayerCompose(
                motionVideoProducer = motionVideoProducer,
                currentFrame = currentFrame,
                onFrameChange = { currentFrame = it },
                onBeforePlay = {
                    val hasPending = onCheckPendingDownloads(project.sdui.toString())
                    if (hasPending) {
                        onNavigateToAssetDownload(project.id)
                        false
                    } else {
                        true
                    }
                },
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            )

            // Draggable Handle
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .pointerInput(density, minTimelineHeight, maxTimelineHeight) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val dragAmountDp = with(density) { dragAmount.y.toDp() }
                                timelineHeight =
                                    (timelineHeight - dragAmountDp).coerceIn(
                                        minTimelineHeight,
                                        maxTimelineHeight,
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
                onFrameChange = { currentFrame = it },
                onResize = { dragAmount ->
                    val dragAmountDp = with(density) { dragAmount.toDp() }
                    timelineHeight =
                        (timelineHeight - dragAmountDp).coerceIn(
                            minTimelineHeight,
                            maxTimelineHeight,
                        )
                },
                fps = provideCurrentConfig().fps,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(timelineHeight)
                        .navigationBarsPadding(),
            )
        }

        // Overlay Back Button
        IconButton(
            onClick = onBackClick,
            modifier =
                Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = CircleShape,
                    ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Overlay Save Button (Tick)
        IconButton(
            onClick = {
                val hasPending = onCheckPendingDownloads(project.sdui.toString())
                if (hasPending) {
                    onNavigateToAssetDownload(project.id)
                } else {
                    onSaveClick(project)
                }
            },
            modifier =
                Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = CircleShape,
                    ),
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Save",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("ktlint:standard:function-naming")
fun PreviewMotionEditorScreen() {
    val sampleProject =
        MotionProject(
            id = "sample_project",
            name = "Sample Project",
            path = "/sample_project",
            sdui =
                JsonObject().apply {
                    // Minimal SDUI structure to satisfy factory/utils if needed
                },
            syncTracker = SyncTracker(updatedBy = "preview_device"),
        )

    MaterialTheme {
        Surface {
            MotionEditorScreen(
                project = sampleProject,
                onBackClick = {},
                onSaveClick = {},
                onNavigateToAssetDownload = {},
                onCheckPendingDownloads = { false },
            )
        }
    }
}
