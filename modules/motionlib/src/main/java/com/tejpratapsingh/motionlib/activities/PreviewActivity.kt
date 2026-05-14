package com.tejpratapsingh.motionlib.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.ui.custom.video.MotionVideoPlayer

abstract class PreviewActivity : ComponentActivity() {
    val motionVideoPlayer by lazy { MotionVideoPlayer(applicationContext, getMotionVideo()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(motionVideoPlayer)

        ViewCompat.setOnApplyWindowInsetsListener(motionVideoPlayer) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = insets.left,
                top = insets.top,
                right = insets.right,
                bottom = insets.bottom,
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    abstract fun getMotionVideo(): MotionVideoProducer
}
