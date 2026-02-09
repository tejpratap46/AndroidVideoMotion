package com.tejpratapsingh.lyricsmaker.presentation.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tejpratapsingh.lyricsmaker.presentation.compose.AppNavHost
import com.tejpratapsingh.lyricsmaker.presentation.ui.theme.AnimatorTheme
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsUiState
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.worker.LyricsMotionWorker
import com.tejpratapsingh.motion.metadataextractor.presentation.ShareReceiverActivity
import kotlinx.coroutines.launch

class SearchActivity : ComponentActivity() {
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

        setContent {
            AnimatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(
                        viewModel = lyricsViewModel,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }

        ShareReceiverActivity.readMetadataFromIntent(intent)?.let {
            lyricsViewModel.socialMeta.value = it
            lyricsViewModel.query.value = it.title ?: it.description ?: ""
            lyricsViewModel.searchLyrics(it.title ?: it.description ?: "")
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                lyricsViewModel.uiState.collect {
                    when (it) {
                        is LyricsUiState.Error -> {
                            Toast
                                .makeText(this@SearchActivity, "Error: ${it.message}", Toast.LENGTH_LONG)
                                .show()
                        }

                        is LyricsUiState.Initial -> {
                        }

                        is LyricsUiState.Loading -> {
                        }

                        is LyricsUiState.Success -> {
                            Toast
                                .makeText(
                                    this@SearchActivity,
                                    "Success ${it.lyrics.size}",
                                    Toast.LENGTH_LONG,
                                ).show()
                        }
                    }
                }
            }
        }
    }
}
