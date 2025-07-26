package com.tejpratapsingh.animator.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.tejpratapsingh.animator.presentation.sampleMotionVideo
import com.tejpratapsingh.animator.worker.SampleMotionWorker
import com.tejpratapsingh.motionlib.activities.PreviewActivity
import com.tejpratapsingh.motionlib.core.MotionVideo

class MotionPreviewActivity : PreviewActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SampleMotionWorker.startWork(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }
    }

    override fun getMotionVideo(): MotionVideo {
        return sampleMotionVideo(applicationContext)
    }
}