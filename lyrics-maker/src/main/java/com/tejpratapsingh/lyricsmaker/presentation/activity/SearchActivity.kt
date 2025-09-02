package com.tejpratapsingh.lyricsmaker.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.tejpratapsingh.lyricsmaker.presentation.compose.SearchScreen
import com.tejpratapsingh.lyricsmaker.presentation.ui.theme.AnimatorTheme
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.worker.LyricsMotionWorker

class SearchActivity : ComponentActivity() {

    private val lyricsViewModel: LyricsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LyricsMotionWorker.cancelAllWork(applicationContext)
        setContent {
            AnimatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SearchScreen(
                        viewModel = lyricsViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}