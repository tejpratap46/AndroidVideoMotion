package com.tejpratapsingh.lyricsmaker.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tejpratapsingh.lyricsmaker.R

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }
}