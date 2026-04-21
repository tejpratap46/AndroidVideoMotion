package com.tejpratapsingh.lyricsmaker.presentation.activity

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import timber.log.Timber
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tejpratapsingh.lyricsmaker.R
import com.tejpratapsingh.lyricsmaker.asLyricsApp
import com.tejpratapsingh.lyricsmaker.presentation.compose.AppNavHost
import com.tejpratapsingh.lyricsmaker.presentation.compose.Screen
import com.tejpratapsingh.lyricsmaker.presentation.ui.theme.AnimatorTheme
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsUiState
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.ProjectsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.ProjectsViewModelFactory
import com.tejpratapsingh.lyricsmaker.presentation.worker.LyricsMotionWorker
import com.tejpratapsingh.motion.metadataextractor.presentation.ShareReceiverActivity
import com.tejpratapsingh.motionstore.extensions.createProjectFile
import com.tejpratapsingh.motionstore.tables.MotionProject
import com.tejpratapsingh.motionstore.worker.SyncWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLConnection

class SearchActivity : ComponentActivity() {
    private val projectsViewModel: ProjectsViewModel by viewModels {
        ProjectsViewModelFactory(
            applicationContext.asLyricsApp().motionStoreDao,
        )
    }

    private val lyricsViewModel: LyricsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LyricsMotionWorker.cancelAllWork(applicationContext)

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

        val metadata = ShareReceiverActivity.readMetadataFromIntent(intent)
        metadata?.let {
            lyricsViewModel.socialMeta.value = it
            lyricsViewModel.query.value = it.title ?: it.description ?: ""
            lyricsViewModel.searchLyrics(it.title ?: it.description ?: "")
        }

        setContent {
            val navController = rememberNavController()
            LaunchedEffect(metadata) {
                if (metadata != null) {
                    navController.navigate(Screen.Search.route)
                }
            }

            AnimatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        projectsViewModel = projectsViewModel,
                        onProjectClick = { motionProject ->
                            navController.navigate(Screen.ProjectDetails.createRoute(motionProject.id))
                        },
                        lyricsViewModel = lyricsViewModel,
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

    /**
     * Share Project
     */
    private fun shareProjectFile(motionProject: MotionProject) {
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
                Toast
                    .makeText(
                        this@SearchActivity,
                        "Success ${lyricsState.lyrics.size}",
                        Toast.LENGTH_LONG,
                    ).show()
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
