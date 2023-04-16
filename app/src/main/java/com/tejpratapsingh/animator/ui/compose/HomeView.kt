package com.tejpratapsingh.animator.ui.compose

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.tejpratapsingh.animator.activities.MotionPreviewActivity
import com.tejpratapsingh.animator.ui.theme.AnimatorTheme
import com.tejpratapsingh.animator.worker.SampleMotionWorker

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val applicationContext = LocalContext.current.applicationContext

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
//                SampleMotionWorker.startWork(applicationContext)
                context.startActivity(Intent(context, MotionPreviewActivity::class.java))
            }
        ) {
            Text(text = "Render Video")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    AnimatorTheme {
        HomeScreen()
    }
}