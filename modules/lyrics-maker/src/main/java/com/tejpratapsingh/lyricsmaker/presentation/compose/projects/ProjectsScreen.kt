package com.tejpratapsingh.lyricsmaker.presentation.compose.projects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.lyricsmaker.R
import com.tejpratapsingh.lyricsmaker.presentation.compose.common.GradientText
import com.tejpratapsingh.motionstore.dao.MotionProjectDao
import com.tejpratapsingh.motionstore.tables.MotionProject

@Composable
fun ProjectsScreen(
    projects: List<MotionProject>,
    isRefreshing: Boolean,
    sortOrder: String,
    onSortOrderChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onCreateNew: () -> Unit,
    onProjectClick: (MotionProject) -> Unit,
    onDeleteProject: (MotionProject) -> Unit,
    onShareProject: (MotionProject) -> Unit,
    onSync: (MotionProject) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        GradientText(text = stringResource(R.string.app_name))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            Box {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier =
                                        Modifier
                                            .clickable { showSortMenu = true }
                                            .padding(8.dp),
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.Sort,
                                        contentDescription = "Sort",
                                    )
                                    Text(
                                        "Sort",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Created On") },
                                        onClick = {
                                            onSortOrderChange(MotionProjectDao.COL_CREATED)
                                            showSortMenu = false
                                        },
                                        trailingIcon = {
                                            if (sortOrder == MotionProjectDao.COL_CREATED) {
                                                Icon(Icons.Rounded.Check, contentDescription = null)
                                            }
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Updated On") },
                                        onClick = {
                                            onSortOrderChange(MotionProjectDao.COL_UPDATED)
                                            showSortMenu = false
                                        },
                                        trailingIcon = {
                                            if (sortOrder == MotionProjectDao.COL_UPDATED) {
                                                Icon(Icons.Rounded.Check, contentDescription = null)
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    CreateNewProjectCard(onClick = onCreateNew)
                }
                items(items = projects, key = { it.id }) { project ->
                    val onProjectClickState by rememberUpdatedState(onProjectClick)
                    val onDeleteProjectState by rememberUpdatedState(onDeleteProject)
                    val onShareProjectState by rememberUpdatedState(onShareProject)
                    val onSyncState by rememberUpdatedState(onSync)

                    ProjectCard(
                        project = project,
                        onClick = { onProjectClickState(project) },
                        onDelete = { onDeleteProjectState(project) },
                        onShare = { onShareProjectState(project) },
                        onSync = { onSyncState(project) },
                    )
                }
            }
        }
    }
}
