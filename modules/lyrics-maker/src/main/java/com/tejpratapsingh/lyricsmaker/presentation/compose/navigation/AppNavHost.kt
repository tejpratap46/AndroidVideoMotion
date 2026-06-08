package com.tejpratapsingh.lyricsmaker.presentation.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tejpratapsingh.lyricsmaker.asLyricsApp
import com.tejpratapsingh.lyricsmaker.domain.ensureArrayList
import com.tejpratapsingh.lyricsmaker.presentation.compose.details.ProjectDetailsScreen
import com.tejpratapsingh.lyricsmaker.presentation.compose.lyrics.SyncedLyricsSelector
import com.tejpratapsingh.lyricsmaker.presentation.compose.projects.ProjectsRoute
import com.tejpratapsingh.lyricsmaker.presentation.compose.search.SearchScreen
import com.tejpratapsingh.lyricsmaker.presentation.compose.templates.LyricsTemplateSelector
import com.tejpratapsingh.lyricsmaker.presentation.motion.extractLyricsTemplateData
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.ProjectsViewModel
import com.tejpratapsingh.motioneditor.ui.MotionEditorScreen
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.VideoAspectRatio
import com.tejpratapsingh.motionlib.core.extensions.md5
import com.tejpratapsingh.motionlib.templates.sdui.MotionTemplateSDUIProvider
import com.tejpratapsingh.motionstore.tables.MotionProject

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
                onBack = { navController.popBackStack() },
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

                    projectsViewModel.loadProjects()

                    navController.navigate(Screen.TemplateSelector.createRoute(projectId))
                },
            )
        }

        composable(route = Screen.TemplateSelector.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            val projects = projectsViewModel.projects.collectAsStateWithLifecycle()
            val project = projects.value.find { it.id == projectId }

            project?.let {
                LyricsTemplateSelector(
                    project = it,
                    onBack = { navController.popBackStack() },
                    onTemplateSelected = { template ->
                        it.metadata.addProperty("template", template.name)
                        // Clear old SDUI if any
                        it.metadata.remove("sdui")

                        val templateData = extractLyricsTemplateData(it)

                        val config =
                            MotionConfig(
                                aspectRatio = VideoAspectRatio.Ratio9x16_480,
                                fps = 24,
                            )

                        // Generate and save SDUI
                        val sdui =
                            MotionTemplateSDUIProvider.provideSDUI(
                                context = navController.context,
                                template = template,
                                data = templateData,
                                config = config,
                            )

                        val updatedProject = it.copy(sdui = sdui)

                        navController.context
                            .asLyricsApp()
                            .motionStoreDao
                            .upsert(updatedProject)

                        projectsViewModel.loadProjects()

                        navController.navigate(Screen.VideoEditor.createRoute(it.id)) {
                            // Pop the template selector so back from details goes to lyrics
                            popUpTo(Screen.TemplateSelector.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = modifier,
                )
            }
        }

        composable(route = Screen.VideoEditor.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            val projects = projectsViewModel.projects.collectAsStateWithLifecycle()
            val project = projects.value.find { it.id == projectId }

            project?.let {
                MotionEditorScreen(
                    project = it,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { updatedProject ->
                        navController.context
                            .asLyricsApp()
                            .motionStoreDao
                            .upsert(updatedProject)

                        projectsViewModel.loadProjects()
                        navController.navigate(Screen.ProjectDetails.createRoute(updatedProject.id)) {
                            popUpTo(Screen.Projects.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    modifier = modifier,
                )
            }
        }

        composable(route = Screen.ProjectDetails.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            val projects = projectsViewModel.projects.collectAsStateWithLifecycle()
            val project = projects.value.find { it.id == projectId }

            project?.let {
                ProjectDetailsScreen(
                    project = it,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { p ->
                        navController.navigate(Screen.VideoEditor.createRoute(p.id)) {
                            launchSingleTop = true
                        }
                    },
                    onShareClick = { p -> projectsViewModel.shareProject(p) },
                    modifier = modifier,
                )
            }
        }
    }
}
