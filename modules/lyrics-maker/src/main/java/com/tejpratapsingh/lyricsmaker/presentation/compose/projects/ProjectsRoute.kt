package com.tejpratapsingh.lyricsmaker.presentation.compose.projects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.ProjectsViewModel
import com.tejpratapsingh.motionstore.tables.MotionProject

@Composable
@Suppress("ktlint:standard:function-naming")
fun ProjectsRoute(
    viewModel: ProjectsViewModel,
    onCreateNew: () -> Unit,
    onProjectClick: (MotionProject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

    val context = LocalContext.current

    ProjectsScreen(
        projects = projects,
        isRefreshing = isRefreshing,
        sortOrder = sortOrder,
        onSortOrderChange = viewModel::updateSortOrder,
        onRefresh = viewModel::refresh,
        onCreateNew = onCreateNew,
        onProjectClick = onProjectClick,
        onDeleteProject = { project ->
            viewModel.deleteProject(context, project)
        },
        onShareProject = viewModel::shareProject,
        onSync = viewModel::syncProject,
        modifier = modifier,
    )
}
