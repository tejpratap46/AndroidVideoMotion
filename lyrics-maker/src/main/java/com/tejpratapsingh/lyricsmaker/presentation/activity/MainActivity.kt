package com.tejpratapsingh.lyricsmaker.presentation.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.tejpratapsingh.lyricsmaker.databinding.ActivityMainBinding
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.lyricsmaker.presentation.worker.LyricsMotionWorker
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private val lyricsViewModel: LyricsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

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

        LyricsMotionWorker.cancelAllWork(applicationContext)

        binding.btnSearch.setOnClickListener {
            binding.btnSearch.isEnabled = false
            lifecycleScope.launch {
                val search = binding.etSearch.text.toString()
                lyricsViewModel.fetchLyrics(search)
            }
        }

        lyricsViewModel.lyricsList.observe(this) {
            binding.btnSearch.isEnabled = true
            it.firstOrNull()?.let { lyricsResponse ->
                LyricsActivity.start(
                    this@MainActivity,
                    lyricsResponse
                )
            }
        }
    }
}