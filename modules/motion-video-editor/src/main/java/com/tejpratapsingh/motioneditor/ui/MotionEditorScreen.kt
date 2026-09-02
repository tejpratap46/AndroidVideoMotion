package com.tejpratapsingh.motioneditor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.tejpratapsingh.motion.sdui.infra.getMotionAudios
import com.tejpratapsingh.motion.sdui.infra.getMotionConfig
import com.tejpratapsingh.motion.sdui.infra.getMotionPlugins
import com.tejpratapsingh.motion.sdui.infra.updateMotionConfig
import com.tejpratapsingh.motioneditor.TimelineItem
import com.tejpratapsingh.motioneditor.ui.compact.MotionEditorCompact
import com.tejpratapsingh.motioneditor.ui.expanded.MotionEditorExpanded
import com.tejpratapsingh.motioneditor.utils.TimelineUtils
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
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

    var draftProject by remember(project) { mutableStateOf(project) }

    val producerFactory = remember { SDUIMotionVideoProducerFactory(context) }
    val motionVideoProducer =
        remember(draftProject) {
            producerFactory.createFromProject(draftProject)
        }
    val timelineTracks =
        remember(draftProject) {
            TimelineUtils.fromSdui(context, draftProject.sdui)
        }

    var currentFrame by remember { mutableIntStateOf(0) }
    var timelineHeight by remember { mutableStateOf(300.dp) }
    var showAspectRatioMenu by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    var selectedItem by remember { mutableStateOf<TimelineItem?>(null) }

    LaunchedEffect(timelineTracks) {
        selectedItem?.let { currentSelection ->
            val newItem = timelineTracks.flatMap { it.items }.find { it.id == currentSelection.id }
            if (newItem != null) {
                selectedItem = newItem
            }
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val isWideScreen =
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED ||
            adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM

    val handleViewSduiUpdate: (JsonObject) -> Unit = { updatedViewSdui ->
        val newRootSdui = draftProject.sdui.deepCopy()
        if (newRootSdui.has("views") && newRootSdui.get("views").isJsonArray) {
            val viewsArray = newRootSdui.get("views").asJsonArray
            val index =
                timelineTracks.indexOfFirst { track ->
                    track.items.any { it.id == selectedItem?.id }
                }
            if (index != -1 && index < viewsArray.size()) {
                viewsArray.set(index, updatedViewSdui)
                draftProject = draftProject.copy(sdui = newRootSdui)
                refreshKey++
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = draftProject.name,
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
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(onClick = { showAspectRatioMenu = true }) {
                            Icon(
                                Icons.Rounded.AspectRatio,
                                contentDescription = "Change Aspect Ratio"
                            )
                        }

                        DropdownMenu(
                            expanded = showAspectRatioMenu,
                            onDismissRequest = { showAspectRatioMenu = false }
                        ) {
                            VideoAspectRatio.all().forEach { ratio ->
                                DropdownMenuItem(
                                    text = { Text(ratio.label) },
                                    onClick = {
                                        showAspectRatioMenu = false
                                        val currentConfig = draftProject.sdui.getMotionConfig()
                                            ?: com.tejpratapsingh.motionlib.core.MotionConfig()
                                        val newConfig = currentConfig.copy(aspectRatio = ratio)

                                        val newSdui = draftProject.sdui.deepCopy()
                                        newSdui.updateMotionConfig(newConfig)

                                        draftProject = draftProject.copy(sdui = newSdui)
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            val hasPending = onCheckPendingDownloads(draftProject.sdui.toString())
                            if (hasPending) {
                                onNavigateToAssetDownload(draftProject.id)
                            } else {
                                onSaveClick(draftProject)
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
                    project = draftProject,
                    motionVideoProducer = motionVideoProducer,
                    timelineTracks = timelineTracks,
                    currentFrame = currentFrame,
                    onFrameChange = { currentFrame = it },
                    selectedItem = selectedItem,
                    onItemClick = { selectedItem = it },
                    timelineHeight = timelineHeight,
                    onTimelineHeightChange = { timelineHeight = it },
                    minTimelineHeight = minTimelineHeight,
                    maxTimelineHeight = maxTimelineHeight,
                    refreshKey = refreshKey,
                    onRefresh = handleViewSduiUpdate,
                    onNavigateToAssetDownload = onNavigateToAssetDownload,
                    onCheckPendingDownloads = onCheckPendingDownloads,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                MotionEditorCompact(
                    project = draftProject,
                    motionVideoProducer = motionVideoProducer,
                    timelineTracks = timelineTracks,
                    currentFrame = currentFrame,
                    onFrameChange = { currentFrame = it },
                    onItemClick = {
                        selectedItem = it
                        showBottomSheet = true
                    },
                    timelineHeight = timelineHeight,
                    onTimelineHeightChange = { timelineHeight = it },
                    minTimelineHeight = minTimelineHeight,
                    maxTimelineHeight = maxTimelineHeight,
                    refreshKey = refreshKey,
                    onRefresh = { refreshKey++ },
                    onNavigateToAssetDownload = onNavigateToAssetDownload,
                    onCheckPendingDownloads = onCheckPendingDownloads,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (showBottomSheet && selectedItem != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                val original = selectedItem?.original
                val sdui = selectedItem?.sdui
                if (original is MotionView && sdui != null) {
                    PropertyEditor(
                        motionView = original,
                        sdui = sdui,
                        onRefresh = handleViewSduiUpdate,
                    )
                }
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
