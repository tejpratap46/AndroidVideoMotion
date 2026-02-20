package com.tejpratapsingh.lyricsmaker.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tejpratapsingh.lyricsmaker.domain.ensureArrayList
import com.tejpratapsingh.lyricsmaker.presentation.activity.LyricsActivity
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.ProjectsViewModel
import com.tejpratapsingh.motionstore.tables.MotionProject

sealed class Screen(
    val route: String,
) {
    object Projects : Screen("projects")

    object Search : Screen("search")

    object Lyrics : Screen("lyrics")
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
                    LyricsActivity.start(
                        context = navController.context,
                        song = lyricsViewModel.selectedSongName,
                        lyrics = lyricsViewModel.selectedLyrics.ensureArrayList(),
                        socialMeta = lyricsViewModel.socialMeta.value,
                    )
                },
            )
        }
    }
}
