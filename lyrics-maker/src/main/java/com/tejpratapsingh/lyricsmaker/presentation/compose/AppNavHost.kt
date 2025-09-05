package com.tejpratapsingh.lyricsmaker.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tejpratapsingh.lyricsmaker.domain.ensureArrayList
import com.tejpratapsingh.lyricsmaker.presentation.activity.LyricsActivity
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Lyrics : Screen("lyrics")
}

@Composable
fun AppNavHost(viewModel: LyricsViewModel, modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(route = Screen.Home.route) {
            SearchScreen(
                viewModel = viewModel,
                modifier = modifier,
                onLyricsSelected = {
                    viewModel.selectedLyricResponse = it
                    navController.navigate(Screen.Lyrics.route)
                }
            )
        }

        composable(route = Screen.Lyrics.route) {
            SyncedLyricsSelector(
                viewModel = viewModel,
                modifier = modifier,
                onSelectionChanged = { selectedLyrics ->
                    viewModel.selectedLyrics = selectedLyrics
                },
                onFinalize = {
                    LyricsActivity.start(
                        context = navController.context,
                        song = viewModel.selectedLyricResponse.trackName,
                        lyrics = viewModel.selectedLyrics.ensureArrayList(),
                    )
                }
            )
        }
    }
}
