package com.tejpratapsingh.motioneditor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.infra.SDUIMotionVideoProducerFactory
import com.tejpratapsingh.motioneditor.ui.compact.MotionEditorCompact
import com.tejpratapsingh.motioneditor.ui.expanded.MotionEditorExpanded
import com.tejpratapsingh.motioneditor.utils.TimelineUtils
import com.tejpratapsingh.motionstore.tables.MotionProject
import com.tejpratapsingh.motionstore.tables.SyncTracker

@OptIn(ExperimentalMaterial3Api::class)
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

    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val isWideScreen =
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED ||
            adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Video Editor",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
                            .padding(end = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape,
                            ),
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        BoxWithConstraints(
            modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val maxHeight = maxHeight
            val minTimelineHeight = 150.dp
            val maxTimelineHeight = if (isWideScreen) maxHeight - 200.dp else maxHeight - 150.dp

            if (isWideScreen) {
                MotionEditorExpanded(
                    project = project,
                    motionVideoProducer = motionVideoProducer,
                    timelineTracks = timelineTracks,
                    currentFrame = currentFrame,
                    onFrameChange = { currentFrame = it },
                    timelineHeight = timelineHeight,
                    onTimelineHeightChange = { timelineHeight = it },
                    minTimelineHeight = minTimelineHeight,
                    maxTimelineHeight = maxTimelineHeight,
                    onNavigateToAssetDownload = onNavigateToAssetDownload,
                    onCheckPendingDownloads = onCheckPendingDownloads,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                MotionEditorCompact(
                    project = project,
                    motionVideoProducer = motionVideoProducer,
                    timelineTracks = timelineTracks,
                    currentFrame = currentFrame,
                    onFrameChange = { currentFrame = it },
                    timelineHeight = timelineHeight,
                    onTimelineHeightChange = { timelineHeight = it },
                    minTimelineHeight = minTimelineHeight,
                    maxTimelineHeight = maxTimelineHeight,
                    onNavigateToAssetDownload = onNavigateToAssetDownload,
                    onCheckPendingDownloads = onCheckPendingDownloads,
                    modifier = Modifier.fillMaxSize()
                )
            }
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
        androidx.compose.material3.Surface {
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
