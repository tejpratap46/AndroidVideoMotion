package com.tejpratapsingh.lyricsmaker.presentation.activity

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tejpratapsingh.lyricsmaker.R
import com.tejpratapsingh.lyricsmaker.asLyricsApp
import com.tejpratapsingh.lyricsmaker.presentation.compose.navigation.AppNavHost
import com.tejpratapsingh.lyricsmaker.presentation.compose.navigation.Screen
import com.tejpratapsingh.lyricsmaker.presentation.ui.theme.AnimatorTheme
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsUiState
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.ProjectsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.ProjectsViewModelFactory
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.SettingsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.worker.LyricsMotionWorker
import com.tejpratapsingh.motion.download.MotionDownloadManager
import com.tejpratapsingh.motion.download.ui.MotionDownloadViewModel
import com.tejpratapsingh.motion.metadataextractor.presentation.ShareReceiverActivity
import com.tejpratapsingh.motionstore.extensions.copyProjectNameToClipboard
import com.tejpratapsingh.motionstore.extensions.createProjectFile
import com.tejpratapsingh.motionstore.tables.MotionProject
import com.tejpratapsingh.motionstore.worker.SyncWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.net.URLConnection

class SearchActivity : ComponentActivity() {
    private val projectsViewModel: ProjectsViewModel by viewModels {
        ProjectsViewModelFactory(
            applicationContext.asLyricsApp().motionStoreDao,
            applicationContext.asLyricsApp().preferenceManager,
        )
    }

    private val lyricsViewModel: LyricsViewModel by viewModel()

    private val downloadViewModel: MotionDownloadViewModel by viewModel()

    private val settingsViewModel: SettingsViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0,
                )
            }
        }

        handleIntent(intent)

        setContent {
            val navController = rememberNavController()
            val socialMeta by lyricsViewModel.socialMeta.collectAsState()

            LaunchedEffect(socialMeta) {
                if (socialMeta.title != null || socialMeta.description != null) {
                    // Avoid redundant navigation if we are already on the Search screen
                    if (navController.currentDestination?.route != Screen.Search.route) {
                        navController.navigate(Screen.Search.route) {
                            launchSingleTop = true
                        }
                    }
                }
            }

            AnimatorTheme {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val showBottomBar =
                    currentDestination?.route in
                        listOf(
                            Screen.Projects.route,
                            Screen.Settings.route,
                        )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Rounded.Folder, contentDescription = null) },
                                    label = { Text("Projects") },
                                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Projects.route } == true,
                                    onClick = {
                                        navController.navigate(Screen.Projects.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                                    label = { Text("Settings") },
                                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Settings.route } == true,
                                    onClick = {
                                        navController.navigate(Screen.Settings.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                )
                            }
                        }
                    },
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        projectsViewModel = projectsViewModel,
                        onProjectClick = { motionProject ->
                            navController.navigate(Screen.ProjectDetails.createRoute(motionProject.id))
                        },
                        lyricsViewModel = lyricsViewModel,
                        downloadViewModel = downloadViewModel,
                        settingsViewModel = settingsViewModel,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    lyricsViewModel.uiState.collect {
                        handleLyricsSearch(it)
                    }
                }

                launch {
                    projectsViewModel.shareEvent.collect {
                        shareProjectFile(it)
                    }
                }

                launch {
                    projectsViewModel.syncEvent.collect {
                        Timber.d("onCreate: syncEvent called")
                        Toast.makeText(this@SearchActivity, "Sync", Toast.LENGTH_SHORT).show()
                        SyncWorker.scheduleImmediate(this@SearchActivity)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        LyricsMotionWorker.cancelAllWork(applicationContext)
        intent?.let {
            val metadata = ShareReceiverActivity.readMetadataFromIntent(it)
            metadata?.let { socialMeta ->
                lyricsViewModel.socialMeta.value = socialMeta
                lyricsViewModel.query.value = socialMeta.title ?: socialMeta.description ?: ""
                lyricsViewModel.searchLyrics(socialMeta.title ?: socialMeta.description ?: "")
            }
        }
    }

    /**
     * Share Project
     */
    private fun shareProjectFile(motionProject: MotionProject) {
        copyProjectNameToClipboard(motionProject)

        val videoFile = createProjectFile(motionProject)
        val videoFileUri: Uri =
            FileProvider.getUriForFile(
                this,
                "${this.packageName}.fileprovider",
                videoFile,
            )
        val intent = Intent(Intent.ACTION_SEND)
        intent.setDataAndType(
            videoFileUri,
            URLConnection.guessContentTypeFromName(videoFile.name),
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(Intent.EXTRA_STREAM, videoFileUri)

        startActivity(Intent.createChooser(intent, "Share Video"))
    }

    private fun handleLyricsSearch(lyricsState: LyricsUiState) {
        when (lyricsState) {
            is LyricsUiState.Error -> {
                showErrorDialog(lyricsState)
            }

            is LyricsUiState.Initial -> {
            }

            is LyricsUiState.Loading -> {
            }

            is LyricsUiState.Success -> {
            }
        }
    }

    private fun showErrorDialog(lyricsState: LyricsUiState.Error) {
        val retrySeconds = 3
        val retryText = getString(R.string.retry, retrySeconds)

        val errorDialog =
            MaterialAlertDialogBuilder(this@SearchActivity)
                .setTitle("Error")
                .setMessage(lyricsState.message)
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }.setPositiveButton(retryText) { dialog, _ ->
                    dialog.dismiss()
                    lyricsViewModel.searchLyrics(lyricsViewModel.query.value)
                }.show()

        lifecycleScope.launch {
            val negativeButton = errorDialog.getButton(DialogInterface.BUTTON_POSITIVE)

            for (i in (retrySeconds - 1) downTo 1) {
                delay(1000)
                if (errorDialog.isShowing) {
                    negativeButton.text = getString(R.string.retry, i)
                }
            }

            delay(1000)
            if (errorDialog.isShowing) {
                errorDialog.dismiss()
                lyricsViewModel.searchLyrics(lyricsViewModel.query.value)
            }
        }
    }

    companion object {
    }
}
