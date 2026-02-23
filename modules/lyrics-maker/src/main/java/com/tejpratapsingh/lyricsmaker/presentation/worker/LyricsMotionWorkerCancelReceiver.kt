package com.tejpratapsingh.lyricsmaker.presentation.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import java.util.UUID

class LyricsMotionWorkerCancelReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_CANCEL = "com.tejpratapsingh.lyricsmaker.ACTION_CANCEL"
        const val EXTRA_WORK_ID = "extra_work_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_CANCEL) {
            val workIdString = intent.getStringExtra(EXTRA_WORK_ID)
            if (workIdString != null) {
                val workId = UUID.fromString(workIdString)
                WorkManager.getInstance(context).cancelWorkById(workId)
            }
        }
    }
}
