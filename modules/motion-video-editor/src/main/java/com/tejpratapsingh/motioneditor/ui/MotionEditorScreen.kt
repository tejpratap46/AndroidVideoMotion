package com.tejpratapsingh.motioneditor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.infra.SDUIMotionVideoProducerFactory
import com.tejpratapsingh.motion.sdui.infra.getMotionConfig
import com.tejpratapsingh.motion.sdui.infra.toJson
import com.tejpratapsingh.motioneditor.TimelineItem
import com.tejpratapsingh.motioneditor.ui.compact.MotionEditorCompact
import com.tejpratapsingh.motioneditor.ui.expanded.MotionEditorExpanded
import com.tejpratapsingh.motioneditor.utils.TimelineUtils
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionstore.tables.MotionProject
import com.tejpratapsingh.motionstore.tables.SyncTracker

@Composable
fun MotionUndoRedoToolbar(
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onUndo,
                enabled = canUndo,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Undo,
                    contentDescription = "Undo",
                )
            }
            IconButton(
                onClick = onRedo,
                enabled = canRedo,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Redo,
                    contentDescription = "Redo",
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("ktlint:standard:function-naming")
fun MotionEditorScreen(
    project: MotionProject,
    onBackClick: () -> Unit,
    onSaveClick: (MotionProject) -> Unit,
    onSaveDraft: (MotionProject) -> Unit = {},
    onNavigateToAssetDownload: (String) -> Unit,
    onCheckPendingDownloads: (String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // Draft SDUI state for real-time live preview
    var draftSdui by remember(project.id, project.sdui) { mutableStateOf(project.sdui.deepCopy()) }
    var undoStack by remember(project.id, project.sdui) { mutableStateOf(listOf(project.sdui.deepCopy())) }
    var redoStack by remember(project.id, project.sdui) { mutableStateOf(emptyList<JsonObject>()) }

    var selectedItem by remember { mutableStateOf<TimelineItem?>(null) }
    var showAspectRatioMenu by remember { mutableStateOf(false) }

    val currentConfig = remember(draftSdui) { draftSdui.getMotionConfig() ?: MotionConfig() }
    val currentAspectRatio = currentConfig.aspectRatio

    val availableAspectRatios =
        remember {
            listOf(
                VideoAspectRatio.Ratio9x16_480,
                VideoAspectRatio.Ratio9x16_720,
                VideoAspectRatio.Ratio9x16_1080,
                VideoAspectRatio.Ratio16x9_480,
                VideoAspectRatio.Ratio16x9_720,
                VideoAspectRatio.Ratio16x9_1080,
                VideoAspectRatio.Ratio16x9_1440,
                VideoAspectRatio.Ratio16x9_2160,
                VideoAspectRatio.Ratio1x1_480,
                VideoAspectRatio.Ratio1x1_720,
                VideoAspectRatio.Ratio1x1_1080,
                VideoAspectRatio.Ratio4x3_480,
                VideoAspectRatio.Ratio4x3_576,
                VideoAspectRatio.Ratio4x3_720,
                VideoAspectRatio.Ratio21x9_1080,
                VideoAspectRatio.Ratio21x9_2160,
            )
        }

    val pushDraftSdui: (JsonObject) -> Unit = { newDraft ->
        undoStack = undoStack + draftSdui.deepCopy()
        draftSdui = newDraft
        redoStack = emptyList()
    }

    val handleUndo: () -> Unit = {
        if (undoStack.size > 1) {
            val previous = undoStack.last()
            undoStack = undoStack.dropLast(1)
            redoStack = redoStack + draftSdui.deepCopy()
            draftSdui = previous
        }
    }

    val handleRedo: () -> Unit = {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.last()
            redoStack = redoStack.dropLast(1)
            undoStack = undoStack + draftSdui.deepCopy()
            draftSdui = next
        }
    }

    val producerFactory = remember { SDUIMotionVideoProducerFactory(context) }
    val motionVideoProducer =
        remember(draftSdui) {
            producerFactory.createFromSdui(draftSdui)
        }
    val timelineTracks =
        remember(draftSdui) {
            TimelineUtils.fromSdui(context, draftSdui)
        }

    // Extract selected MotionView JsonObject
    val selectedViewJson: JsonObject? =
        remember(draftSdui, selectedItem) {
            val item = selectedItem
            if (item != null && item.viewIndex >= 0) {
                val viewsArray = draftSdui.getAsJsonArray("views")
                if (viewsArray != null && item.viewIndex in 0 until viewsArray.size()) {
                    viewsArray.get(item.viewIndex).asJsonObject
                } else null
            } else null
        }

    val updateSelectedViewJson: (JsonObject) -> Unit = { updatedViewJson ->
        val item = selectedItem
        if (item != null && item.viewIndex >= 0) {
            val newDraft = draftSdui.deepCopy()
            var viewsArray = newDraft.getAsJsonArray("views")
            if (viewsArray == null) {
                viewsArray = JsonArray()
                newDraft.add("views", viewsArray)
            }
            if (item.viewIndex in 0 until viewsArray.size()) {
                viewsArray.set(item.viewIndex, updatedViewJson)
                pushDraftSdui(newDraft)
            }
        }
    }

    var currentFrame by remember { mutableIntStateOf(0) }
    var timelineHeight by remember { mutableStateOf(300.dp) }

    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val isWideScreen =
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED ||
            adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM

    val undoRedoContent: @Composable () -> Unit = {
        MotionUndoRedoToolbar(
            onUndo = handleUndo,
            onRedo = handleRedo,
            canUndo = undoStack.size > 1,
            canRedo = redoStack.isNotEmpty(),
        )
    }

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
                    Box {
                        IconButton(
                            onClick = { showAspectRatioMenu = true },
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AspectRatio,
                                contentDescription = "Aspect Ratio",
                            )
                        }

                        DropdownMenu(
                            expanded = showAspectRatioMenu,
                            onDismissRequest = { showAspectRatioMenu = false },
                        ) {
                            availableAspectRatios.forEach { ratio ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${ratio.label} (${ratio.width}x${ratio.height})",
                                            style =
                                                if (ratio.width == currentAspectRatio.width && ratio.height == currentAspectRatio.height) {
                                                    MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                } else {
                                                    MaterialTheme.typography.bodyMedium
                                                },
                                        )
                                    },
                                    onClick = {
                                        showAspectRatioMenu = false
                                        val updatedConfig = currentConfig.copy(aspectRatio = ratio)
                                        val newDraft = draftSdui.deepCopy()
                                        newDraft.add("config", updatedConfig.toJson())
                                        pushDraftSdui(newDraft)
                                    },
                                    leadingIcon =
                                        if (ratio.width == currentAspectRatio.width && ratio.height == currentAspectRatio.height) {
                                            {
                                                Icon(
                                                    Icons.Rounded.Check,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        } else {
                                            null
                                        },
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            val updatedProject = project.copy(sdui = draftSdui)
                            onSaveClick(updatedProject)
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
                    draftSdui = draftSdui,
                    motionVideoProducer = motionVideoProducer,
                    timelineTracks = timelineTracks,
                    currentFrame = currentFrame,
                    onFrameChange = { currentFrame = it },
                    timelineHeight = timelineHeight,
                    onTimelineHeightChange = { timelineHeight = it },
                    minTimelineHeight = minTimelineHeight,
                    maxTimelineHeight = maxTimelineHeight,
                    selectedItem = selectedItem,
                    selectedViewJson = selectedViewJson,
                    onItemSelect = { selectedItem = it },
                    onViewJsonChange = updateSelectedViewJson,
                    onDismissEditor = { selectedItem = null },
                    onSaveDraft = onSaveDraft,
                    onNavigateToAssetDownload = onNavigateToAssetDownload,
                    onCheckPendingDownloads = onCheckPendingDownloads,
                    undoRedoContent = undoRedoContent,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                MotionEditorCompact(
                    project = project,
                    draftSdui = draftSdui,
                    motionVideoProducer = motionVideoProducer,
                    timelineTracks = timelineTracks,
                    currentFrame = currentFrame,
                    onFrameChange = { currentFrame = it },
                    timelineHeight = timelineHeight,
                    onTimelineHeightChange = { timelineHeight = it },
                    minTimelineHeight = minTimelineHeight,
                    maxTimelineHeight = maxTimelineHeight,
                    selectedItem = selectedItem,
                    selectedViewJson = selectedViewJson,
                    onItemSelect = { selectedItem = it },
                    onViewJsonChange = updateSelectedViewJson,
                    onDismissEditor = { selectedItem = null },
                    onSaveDraft = onSaveDraft,
                    onNavigateToAssetDownload = onNavigateToAssetDownload,
                    onCheckPendingDownloads = onCheckPendingDownloads,
                    undoRedoContent = undoRedoContent,
                    modifier = Modifier.fillMaxSize(),
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
