package com.tejpratapsingh.lyricsmaker.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tejpratapsingh.lyricsmaker.asLyricsApp
import com.tejpratapsingh.lyricsmaker.domain.ensureArrayList
import com.tejpratapsingh.lyricsmaker.presentation.activity.LyricsActivity
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.ProjectsViewModel
import com.tejpratapsingh.motionlib.core.extensions.md5
import com.tejpratapsingh.motionstore.tables.MotionProject

sealed class Screen(
    val route: String,
) {
    object Projects : Screen("projects")

    object Search : Screen("search")

    object Lyrics : Screen("lyrics")

    object ProjectDetails : Screen("project_details/{projectId}") {
        fun createRoute(projectId: String) = "project_details/$projectId"
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    currentScreen: Screen = Screen.Projects,
    projectsViewModel: ProjectsViewModel,
    onProjectClick: (MotionProject) -> Unit = {},
    lyricsViewModel: LyricsViewModel,
    modifier: Modifier,
) {
    NavHost(navController = navController, startDestination = currentScreen.route) {
        composable(route = Screen.Projects.route) {
            ProjectsRoute(
                viewModel = projectsViewModel,
                onCreateNew = {
                    navController.navigate(Screen.Search.route)
                },
                onProjectClick = onProjectClick,
                modifier = modifier,
            )
        }
        composable(route = Screen.Search.route) {
            SearchScreen(
                viewModel = lyricsViewModel,
                modifier = modifier,
                onLyricsSelected = {
                    lyricsViewModel.selectedLyric.tryEmit(it)
                    navController.navigate(Screen.Lyrics.route)
                },
            )
        }

        composable(route = Screen.Lyrics.route) {
            SyncedLyricsSelector(
                viewModel = lyricsViewModel,
                modifier = modifier,
                onSelectionChanged = { selectedLyrics ->
                    lyricsViewModel.selectedLyrics = selectedLyrics
                },
                onFinalize = {
                    val songName = lyricsViewModel.selectedSongName
                    val projectId = songName.md5()
                    val lyrics = lyricsViewModel.selectedLyrics.ensureArrayList()
                    val image = lyricsViewModel.socialMeta.value.image

                    val project =
                        MotionProject(
                            id = projectId,
                            name = songName,
                            path = "/$projectId",
                            metadata =
                                JsonObject().apply {
                                    addProperty("image", image)
                                    addProperty("startTime", lyricsViewModel.selectedStartTimeInSeconds)
                                    add(
                                        "lyrics",
                                        JsonArray().apply {
                                            lyrics.forEach { frame ->
                                                add(
                                                    JsonObject().apply {
                                                        addProperty("frame", frame.frame)
                                                        addProperty("text", frame.text)
                                                    },
                                                )
                                            }
                                        },
                                    )
                                },
                        )

                    navController.context
                        .asLyricsApp()
                        .motionStoreDao
                        .upsert(project)

                    LyricsActivity.start(
                        context = navController.context,
                        projectId = projectId,
                    )
                },
            )
        }

        composable(route = Screen.ProjectDetails.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            val projects = projectsViewModel.projects.collectAsStateWithLifecycle()
            val project = projects.value.find { it.id == projectId }

            project?.let {
                ProjectDetailsScreen(
                    project = it,
                    onBackClick = { navController.popBackStack() },
                    onShareClick = { p -> projectsViewModel.shareProject(p) },
                    modifier = modifier,
                )
            }
        }
    }
}
