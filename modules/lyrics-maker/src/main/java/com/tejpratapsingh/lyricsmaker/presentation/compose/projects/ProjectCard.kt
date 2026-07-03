package com.tejpratapsingh.lyricsmaker.presentation.compose.projects

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.motionstore.extensions.createProjectFile
import com.tejpratapsingh.motionstore.tables.MotionProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
@Suppress("ktlint:standard:function-naming")
internal fun ProjectCard(
    project: MotionProject,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var thumbnail by remember(project.id, project.updated) {
        mutableStateOf(ThumbnailCache.get("${project.id}_${project.updated}"))
    }

    LaunchedEffect(project.id, project.updated) {
        if (thumbnail == null) {
            withContext(Dispatchers.IO) {
                val projectFile = context.createProjectFile(project)
                if (projectFile.exists()) {
                    val extractedBitmap = extractFirstFrame(projectFile.path)
                    extractedBitmap?.let {
                        ThumbnailCache.removeByPrefix(project.id)
                        ThumbnailCache.put("${project.id}_${project.updated}", it)
                        withContext(Dispatchers.Main) {
                            thumbnail = it
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            projectName = project.name,
            onConfirm = onDelete,
            onDismiss = { showDeleteDialog = false },
        )
    }

    Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Thumbnail background
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VideocamOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    )
                }
            }

            // Scrim so text stays readable over any thumbnail
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(
                                alpha = if (thumbnail != null) 0.45f else 0f,
                            ),
                        ),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Left Item
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp),
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (thumbnail != null) Icons.Rounded.PlayCircle else Icons.Rounded.VideocamOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    // Right Item - Sync button
                    IconButton(
                        onClick = onSync,
                        modifier = Modifier.size(40.dp),
                        colors =
                            IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary,
                            ),
                    ) {
                        Icon(
                            imageVector = if (project.syncTracker.isDirty) Icons.Rounded.Check else Icons.Rounded.DoneAll,
                            contentDescription = if (project.syncTracker.isDirty) "Start sync" else "Synced",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color =
                            if (thumbnail != null) {
                                Color.White
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = project.updatedLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            if (thumbnail != null) {
                                Color.White.copy(
                                    alpha = 0.75f,
                                )
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(32.dp),
                            colors =
                                IconButtonDefaults.iconButtonColors(
                                    contentColor =
                                        if (thumbnail != null) {
                                            Color.White
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Share project",
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(32.dp),
                            colors =
                                IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete project",
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
