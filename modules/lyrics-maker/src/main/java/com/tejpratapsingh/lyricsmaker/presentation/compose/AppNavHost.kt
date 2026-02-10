package com.tejpratapsingh.lyricsmaker.presentation.compose

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.domain.ensureArrayList
import com.tejpratapsingh.lyricsmaker.presentation.activity.LyricsActivity
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.lyricsmaker.utils.getSyncedLyricFrameList
import com.tejpratapsingh.motion.ongoing.domain.CurrentProject

sealed class Screen(
    val route: String,
) {
    object Home : Screen("home")

    object Lyrics : Screen("lyrics")
    object Dashboard : Screen("dashboard")
    object ProjectDetails : Screen("projectDetails")
}

@Composable
fun AppNavHost(
    viewModel: LyricsViewModel,
    modifier: Modifier,
    startDestination: String = Screen.Dashboard.route
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(route = Screen.Home.route) {
            SearchScreen(
                viewModel = viewModel,
                modifier = modifier,
                onLyricsSelected = {
                    viewModel.selectedLyricResponse = it
                    navController.navigate("${Screen.Lyrics.route}/")
                },
            )
        }

        composable(route = Screen.Dashboard.route) {
            DashBoardScreen(
                lyricsViewModel = viewModel,
                navController
            )
        }

        composable(route = Screen.ProjectDetails.route) {
            ProjectDetails(
               lyricsViewModel = viewModel,
                modifier = modifier
            )
        }

        composable(route = "${Screen.Lyrics.route}/{lyrics}") { backStackEntry ->
            val itemsArg = backStackEntry.arguments?.getString("lyrics")
            var lyrics: List<SyncedLyricFrame>? = null
            if (!itemsArg.isNullOrEmpty()) {
                Log.d("ooo", "found lyrics empty")
                lyrics = itemsArg.split(",").let {
                    Log.d("ooo", "found lyrics $it")
                    getSyncedLyricFrameList(it)
                }
            }
            SyncedLyricsSelector(
                lyricsList = lyrics,
                viewModel = viewModel,
                modifier = modifier,
                onSelectionChanged = { selectedLyrics ->
                    viewModel.selectedLyrics = selectedLyrics
                },
                onFinalize = {
                    LyricsActivity.start(
                        context = navController.context,
                        song = viewModel.selectedSongName,
                        lyrics = viewModel.selectedLyrics.ensureArrayList(),
                        socialMeta = viewModel.socialMeta.value,
                    )
                },
            )
        }
    }
}
