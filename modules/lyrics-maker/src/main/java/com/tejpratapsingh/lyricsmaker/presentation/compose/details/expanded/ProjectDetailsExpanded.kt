package com.tejpratapsingh.lyricsmaker.presentation.compose.details.expanded

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.lyricsmaker.presentation.compose.details.ProjectInfoSection
import com.tejpratapsingh.lyricsmaker.presentation.compose.details.VideoPlayerSection
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionstore.tables.MotionProject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsExpanded(
    project: MotionProject,
    motionVideoProducer: MotionVideoProducer?,
    isRendering: Boolean,
    isVideoGenerated: Boolean,
    onBackClick: () -> Unit,
    onEditClick: (MotionProject) -> Unit,
    onShareClick: () -> Unit,
    onGenerateVideoClick: () -> Unit,
    onReRenderClick: () -> Unit,
    onCheckPendingDownloads: (String) -> Boolean,
    onNavigateToAssetDownload: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(project) }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Centered Player Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                VideoPlayerSection(
                    motionVideoProducer = motionVideoProducer,
                    onCheckPendingDownloads = { onCheckPendingDownloads(project.sdui.toString()) },
                    onNavigateToAssetDownload = { onNavigateToAssetDownload(project.id) },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            VerticalDivider(color = Color.White.copy(alpha = 0.1f))

            // Side Info Panel
            Surface(
                modifier = Modifier
                    .width(400.dp)
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                ProjectInfoSection(
                    project = project,
                    isRendering = isRendering,
                    isVideoGenerated = isVideoGenerated,
                    onShareClick = onShareClick,
                    onGenerateVideoClick = onGenerateVideoClick,
                    onReRenderClick = onReRenderClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
