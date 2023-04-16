package com.tejpratapsingh.motionlib.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tejpratapsingh.motionlib.core.MotionVideo
import com.tejpratapsingh.motionlib.ui.MotionVideoPlayer

abstract class PreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(MotionVideoPlayer(applicationContext, getMotionVideo()))
    }

    abstract fun getMotionVideo(): MotionVideo
}