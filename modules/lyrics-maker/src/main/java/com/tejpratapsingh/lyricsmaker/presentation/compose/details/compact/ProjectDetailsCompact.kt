package com.tejpratapsingh.lyricsmaker.presentation.compose.details.compact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.lyricsmaker.presentation.compose.details.ProjectInfoSection
import com.tejpratapsingh.lyricsmaker.presentation.compose.details.VideoPlayerSection
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionstore.tables.MotionProject

@Composable
fun ProjectDetailsCompact(
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
    Box(
        modifier =
        modifier
            .fillMaxSize()
            .background(Color.Black), // Dark background for Theater Mode
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            VideoPlayerSection(
                motionVideoProducer = motionVideoProducer,
                onCheckPendingDownloads = { onCheckPendingDownloads(project.sdui.toString()) },
                onNavigateToAssetDownload = { onNavigateToAssetDownload(project.id) },
                modifier = Modifier.weight(1f),
            )

            ProjectInfoSection(
                project = project,
                isRendering = isRendering,
                isVideoGenerated = isVideoGenerated,
                onShareClick = onShareClick,
                onGenerateVideoClick = onGenerateVideoClick,
                onReRenderClick = onReRenderClick,
            )
        }

        NavigationOverlays(
            onBackClick = onBackClick,
            onEditClick = { onEditClick(project) },
        )
    }
}

@Composable
private fun BoxScope.NavigationOverlays(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    // Overlay Back Button - Positioned at top-left with status bar padding
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

    // Overlay Edit Button - Positioned at top-right with status bar padding
    IconButton(
        onClick = onEditClick,
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
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Edit",
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}
