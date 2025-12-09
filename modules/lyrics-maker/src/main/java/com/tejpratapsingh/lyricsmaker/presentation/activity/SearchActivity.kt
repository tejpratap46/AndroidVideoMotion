package com.tejpratapsingh.lyricsmaker.presentation.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.tejpratapsingh.lyricsmaker.presentation.compose.AppNavHost
import com.tejpratapsingh.lyricsmaker.presentation.ui.theme.AnimatorTheme
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.worker.LyricsMotionWorker
import com.tejpratapsingh.motion.metadataextractor.ShareReceiverActivity
import kotlinx.coroutines.launch

class SearchActivity : ComponentActivity() {

    private val lyricsViewModel: LyricsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LyricsMotionWorker.cancelAllWork(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
                )
            }
        }

        setContent {
            AnimatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(
                        viewModel = lyricsViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        ShareReceiverActivity.readMetadataFromIntent(intent)?.let {
            lyricsViewModel.socialMeta.value = it
            lifecycleScope.launch {
                lyricsViewModel.query.value = it.title ?: it.description ?: ""
                lyricsViewModel.fetchLyrics()
            }
        }
    }
}