package com.tejpratapsingh.lyricsmaker.presentation.compose.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.tejpratapsingh.lyricsmaker.presentation.motion.getLyricsVideoProducer
import com.tejpratapsingh.lyricsmaker.presentation.worker.LyricsMotionWorker
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.ui.custom.video.MotionVideoPlayerCompose
import com.tejpratapsingh.motionstore.extensions.createProjectFile
import com.tejpratapsingh.motionstore.tables.MotionProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.Row

@Composable
@Suppress("ktlint:standard:function-naming")
fun ProjectDetailsScreen(
    project: MotionProject,
    onBackClick: () -> Unit,
    onEditClick: (MotionProject) -> Unit,
    onShareClick: (MotionProject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val motionVideoProducer by produceState<MotionVideoProducer?>(initialValue = null, project) {
        value =
            withContext(Dispatchers.IO) {
                getLyricsVideoProducer(context, project)
            }
    }

    val workInfos by WorkManager
        .getInstance(context)
        .getWorkInfosByTagFlow(LyricsMotionWorker.getWorkTag(project.id))
        .collectAsState(initial = emptyList())

    val isRendering =
        remember(workInfos) {
            workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
        }

    var showReRenderConfirmation by remember { mutableStateOf(false) }

    if (showReRenderConfirmation) {
        AlertDialog(
            onDismissRequest = { showReRenderConfirmation = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReRenderConfirmation = false
                        LyricsMotionWorker.startWork(context, project.id)
                    },
                ) {
                    Text("Re-render")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReRenderConfirmation = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text(text = "Re-render Video?")
            },
            text = {
                Text(text = "Are you sure you want to re-render this video? This will overwrite the existing video file.")
            },
        )
    }

    // Use a fresh Box and ignore the passed 'modifier' (which contains Scaffold padding)
    // to fix the "extra space above" issue and make it truly immersive.
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Video Player Area
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black),
            ) {
                motionVideoProducer?.let {
                    MotionVideoPlayerCompose(
                        motionVideoProducer = it,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // Bottom Info and Share Button
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .navigationBarsPadding(), // Respect bottom navigation bar
                ) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    val startTime = project.metadata.get("startTime")?.asInt ?: 0
                    Text(
                        text = "Starts at: ${startTime}s",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val isVideoGenerated by produceState(initialValue = false, project.id, isRendering) {
                        value =
                            withContext(Dispatchers.IO) {
                                context.createProjectFile(project).exists()
                            }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = {
                                if (isVideoGenerated) {
                                    onShareClick(project)
                                } else {
                                    LyricsMotionWorker.startWork(context, project.id)
                                }
                            },
                            enabled = !isRendering,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Icon(
                                imageVector =
                                    if (isRendering) {
                                        Icons.Rounded.PlayCircle
                                    } else if (isVideoGenerated) {
                                        Icons.Rounded.Share
                                    } else {
                                        Icons.Rounded.PlayCircle
                                    },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text =
                                    if (isRendering) {
                                        "Rendering..."
                                    } else if (isVideoGenerated) {
                                        "Share Project"
                                    } else {
                                        "Generate Video"
                                    },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        if (isVideoGenerated) {
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = {
                                    showReRenderConfirmation = true
                                },
                                enabled = !isRendering,
                                modifier =
                                    Modifier
                                        .size(56.dp)
                                        .background(
                                            color =
                                                if (isRendering) {
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                } else {
                                                    MaterialTheme.colorScheme.secondaryContainer
                                                },
                                            shape = RoundedCornerShape(16.dp),
                                        ),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Re-render",
                                    tint =
                                        if (isRendering) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        },
                                )
                            }
                        }
                    }
                }
            }
        }

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
            onClick = { onEditClick(project) },
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
}
